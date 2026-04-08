package com.example.blockchain.service;

import com.example.blockchain.dto.BlockDto;
import com.example.blockchain.BlockMapper;
import com.example.blockchain.dto.ChainDto;
import com.example.blockchain.dto.StatusDto;
import com.example.blockchain.model.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class PeerService {

    private static final Logger log = LoggerFactory.getLogger(PeerService.class);

    // CopyOnWriteArraySet es thread-safe para lecturas concurrentes
    private final Set<String> peers = new CopyOnWriteArraySet<>();
    private final RestTemplate restTemplate;
    private final BlockchainService blockchainService;

    @Value("${blockchain.seed-peers:}")
    private String seedPeersConfig; // pueden ser varios ej: "http://IP1:8080,http://IP2:8080"

    @Value("${blockchain.my-url:}")
    private String myUrl;

    public PeerService(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
        this.restTemplate = new RestTemplate();
    }
    // Flujo al arrancar:
    // 1. Contactar seed por GET /status
    // 2. Descargar cadena por GET /chain
    // 3. Registrarse con POST /peers
    // 4. Recibir lista de peers

    @EventListener(ApplicationReadyEvent.class)
    public void joinNetwork() {
        if (seedPeersConfig == null || seedPeersConfig.isBlank()) return;
        if (myUrl == null || myUrl.isBlank()) return;
        String normalizedMyUrl = normalizeUrl(myUrl);

        String[] seeds = seedPeersConfig.split(",");
        for (String seed : seeds) {
            String seedUrl = normalizeUrl(seed.trim());

            // Si soy el seed no me conecto a mi
            if (normalizedMyUrl.equals(seedUrl)) {
                log.info("Soy un seed node, esperando conexiones...");
                continue;
            }

            try {
                // 1. Verificar que el seed esta vivo
                StatusDto status = restTemplate.getForObject(seedUrl + "/status", StatusDto.class);

                // 2. Cadena del seed
                ChainDto chainDto = restTemplate.getForObject(seedUrl + "/chain", ChainDto.class);
                if (chainDto != null) {
                    List<Block> theirChain = chainDto.chain().stream()
                            .map(BlockMapper::toModel).toList();
                    blockchainService.replaceChainIfValid(theirChain);
                }

                // 3. Registrarse con el seed, devuelve la lista de peers conocidos
                Map<String, Object> response = restTemplate.postForObject(
                        seedUrl + "/peers",
                        Map.of("url", normalizedMyUrl),
                        Map.class
                );

                // 4. Agregar los peers que devolvio el seed
                if (response != null && response.get("peers") instanceof List<?> peerList) {
                    peerList.forEach(p -> registerPeer(p.toString()));
                }

                registerPeer(seedUrl);
                log.info("Conectado a la red via {}. Peers: {}", seedUrl, peers);

            } catch (Exception e) {
                log.warn("No se pudo conectar al seed {}: {}", seedUrl, e.getMessage());
            }
        }
    }

    public boolean registerPeer(String url) {
        if (url == null || url.isBlank()) return false;
        String normalized = normalizeUrl(url);
        if (myUrl != null && normalized.equals(normalizeUrl(myUrl))) return false; // no autoregistrarse
        boolean added = peers.add(normalized);
        if (added) {
            log.info("Peer registrado: {}", normalized);
        }
        return added;
    }

    public Set<String> getPeers() {
        return Set.copyOf(peers);
    }

    // Propagacion. Cuando minamos un bloque, lo mandamos a todos los peers
    public void broadcastBlock(Block block) {
        BlockDto dto = BlockMapper.toDto(block);
        for (String peer : peers) {
            try {
                restTemplate.postForEntity(peer + "/blocks", dto, Map.class);
                log.info("Bloque #{} enviado a {}", block.getIndex(), peer);
            } catch (Exception e) {
                log.warn("No se pudo enviar bloque a {}: {}", peer, e.getMessage());
            }
        }
    }


    public void broadcastNewPeer(String newPeerUrl) {
        for (String peer : peers) {
            try {
                restTemplate.postForEntity(peer + "/peers", Map.of("url", newPeerUrl), Map.class);
            } catch (Exception e) {
                log.warn("No se pudo notificar nuevo peer a {}: {}", peer, e.getMessage());
            }
        }
    }


    public void syncWithPeers() {
        for (String peer : peers) {
            try {
                ChainDto response = restTemplate.getForObject(peer + "/chain", ChainDto.class);
                if (response == null) continue;

                List<Block> theirChain = response.chain().stream()
                        .map(BlockMapper::toModel)
                        .toList();

                boolean replaced = blockchainService.replaceChainIfValid(theirChain);
                if (replaced) {
                    log.info("Cadena sincronizada desde {}. Longitud: {}", peer, theirChain.size());
                }
            } catch (Exception e) {
                log.warn("No se pudo sincronizar con {}: {}", peer, e.getMessage());
            }
        }
    }

    public String getMyUrl() { return myUrl; }

    private String normalizeUrl(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}