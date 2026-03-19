package com.example.blockchain.service;

import com.example.blockchain.dto.BlockDto;
import com.example.blockchain.BlockMapper;
import com.example.blockchain.dto.ChainDto;
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

    public PeerService(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
        this.restTemplate = new RestTemplate();
    }

    public void registerPeer(String peerUrl) {
        String normalized = normalizeUrl(peerUrl);
        peers.add(normalized);
        log.info("Peer registrado: {}", normalized);
    }

    public Set<String> getPeers() {
        return Set.copyOf(peers);
    }

    // Propagacion. Cuando minamos un bloque, lo mandamos a todos los peers
    public void broadcastBlock(Block block) {
        BlockDto dto = BlockMapper.toDto(block);
        for (String peer : peers) {
            try {
                restTemplate.postForEntity(peer + "/api/block", dto, String.class);
                log.info("Bloque #{} enviado a {}", block.getIndex(), peer);
            } catch (Exception e) {
                log.warn("No se pudo enviar bloque a {}: {}", peer, e.getMessage());
            }
        }
    }

    // Sincronizacion. Al arrancar o cuando se necesite, pedimos la cadena a los peers
    // y la reemplazamos si alguno tiene una mas larga y valida
    public void syncWithPeers() {
        for (String peer : peers) {
            try {
                ChainDto response = restTemplate.getForObject(peer + "/api/chain", ChainDto.class);
                if (response == null) continue;

                List<Block> theirChain = response.chain().stream()
                        .map(BlockMapper::toModel)
                        .toList();

                boolean replaced = blockchainService.replaceChainIfValid(theirChain);
                if (replaced) {
                    log.info("Cadena sincronizada desde {}. Nueva longitud: {}", peer, theirChain.size());
                }
            } catch (Exception e) {
                log.warn("No se pudo sincronizar con {}: {}", peer, e.getMessage());
            }
        }
    }

    private String normalizeUrl(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    @Value("${blockchain.seed-url:}")
    private String seedUrl;

    @Value("${blockchain.my-url:}")
    private String myUrl;

    // Se ejecuta automáticamente cuando Spring termina de iniciar la app
    @EventListener(ApplicationReadyEvent.class)
    public void joinNetwork() {
        if (seedUrl.isBlank() || myUrl.isBlank()) return;
        // Si soy el seed no me conecto a nadie
        if (myUrl.equals(seedUrl)) {
            log.info("Soy el seed node, esperando conexiones...");
            return;
        }
        try {
            // Le aviso al seed que existo
            Map<String, Object> response = restTemplate.postForObject(
                    seedUrl + "/api/peers/join",
                    Map.of("url", myUrl),
                    Map.class
            );
            // Agrego los peers que me devolvió el seed
            List<String> knownPeers = (List<String>) response.get("peers");
            knownPeers.forEach(this::registerPeer);
            // También agrego el seed como peer
            registerPeer(seedUrl);
            // Sincronizo la cadena
            syncWithPeers();
            log.info("Conectado a la red. Peers conocidos: {}", peers);
        } catch (Exception e) {
            log.warn("No se pudo conectar al seed {}: {}", seedUrl, e.getMessage());
        }
    }

    public void broadcastNewPeer(String newPeerUrl) {
        for (String peer : peers) {
            try {
                restTemplate.postForEntity(
                        peer + "/api/peers",
                        Map.of("url", newPeerUrl),
                        String.class
                );
            } catch (Exception e) {
                log.warn("No se pudo notificar a {}: {}", peer, e.getMessage());
            }
        }
    }
}