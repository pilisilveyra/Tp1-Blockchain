package com.example.blockchain.service;

import com.example.blockchain.dto.BlockDto;
import com.example.blockchain.BlockMapper;
import com.example.blockchain.dto.ChainDto;
import com.example.blockchain.model.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
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
}