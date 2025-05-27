package com.kids.mutex;

import com.kids.app.servent.ServentIdentity;
import com.kids.avro.ServentIdentityAvro;
import com.kids.avro.SuzukiKasamiTokenPayloadAvro;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Suzuki-Kasami Token class
 * <p>
 * This class represents the token used in the Suzuki-Kasami distributed mutual exclusion algorithm.
 * It contains a list of logical clocks (LN) and a queue of nodes waiting for the token.
 * <p>
 * The class is thread-safe and can be used in a distributed system.
 */
@Getter
@Setter
public class SuzukiKasamiToken implements Serializable {

    private List<Integer> LN;
    private Queue<ServentIdentity> queue;

    public SuzukiKasamiToken(int numNodes) {
        this.LN = new ArrayList<>();
        this.queue = new LinkedList<>();

        IntStream.range(0, numNodes).forEach(i -> LN.add(0));
    }

    // From Avro
    public SuzukiKasamiToken(SuzukiKasamiTokenPayloadAvro avroPayload) {
        this.LN = new ArrayList<>(avroPayload.getLN());
        this.queue = avroPayload.getQueue().stream()
                .map(avroId -> new ServentIdentity(avroId.getIp(), avroId.getPort()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    // To Avro
    public SuzukiKasamiTokenPayloadAvro toAvroPayload() {
        List<ServentIdentityAvro> queueAvro = this.queue.stream()
                .map(id -> ServentIdentityAvro.newBuilder().setIp(id.ip()).setPort(id.port()).build())
                .collect(Collectors.toList());
        return SuzukiKasamiTokenPayloadAvro.newBuilder()
                .setLN(new ArrayList<>(this.LN))
                .setQueue(queueAvro)
                .build();
    }

    public void removeNodeFromQueue(ServentIdentity serventIdentity) {
        queue.remove(serventIdentity);
    }
}
