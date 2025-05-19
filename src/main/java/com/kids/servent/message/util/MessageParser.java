package com.kids.servent.message.util;

import com.kids.app.servent.ServentIdentity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public final class MessageParser {

    private final List<ServentIdentity> nodes;
    private final List<Integer> requestNumbers;

    public static MessageParser fromString(String messageText) {
        String[] parts = messageText.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid update message format");
        }

        List<ServentIdentity> nodes = parseNodes(parts[0]);
        List<Integer> rns = parseRequestNumbers(parts[1]);

        return new MessageParser(nodes, rns);
    }

    private static List<ServentIdentity> parseNodes(String nodesText) {
        List<ServentIdentity> result = new ArrayList<>();
        String[] nodes = nodesText.split(",");

        for (String nodeStr : nodes) {
            if (nodeStr.isBlank() || nodeStr.strip().equals("_")) continue;
            String[] nodeInfo = nodeStr.split(":");
            if (nodeInfo.length == 2) {
                result.add(new ServentIdentity(nodeInfo[0], Integer.parseInt(nodeInfo[1])));
            }
        }

        return result;
    }

    private static List<Integer> parseRequestNumbers(String rnsText) {
        String cleanText = rnsText.replace("|", "").trim();
        if (cleanText.isEmpty()) return new ArrayList<>();

        return Arrays.stream(cleanText.split(","))
                .map(Integer::parseInt)
                .toList();
    }

    public String serialize(ServentIdentity additionalNode, List<Integer> updatedRns) {
        StringBuilder nodesPart = new StringBuilder();
        for (ServentIdentity node : nodes) {
            nodesPart.append(node.ip()).append(":").append(node.port()).append(",");
        }
        nodesPart.append(additionalNode.ip()).append(":").append(additionalNode.port());

        StringBuilder rnPart = new StringBuilder("|");
        for (Integer rn : updatedRns)
            rnPart.append(rn).append(",");

        if (rnPart.length() > 1) rnPart.setLength(rnPart.length() - 1);
        rnPart.append("|");

        return nodesPart + "-" + rnPart;
    }

}
