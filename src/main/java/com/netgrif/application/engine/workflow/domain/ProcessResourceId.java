package com.netgrif.application.engine.workflow.domain;

import lombok.Getter;
import org.bson.types.ObjectId;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

@Getter
public final class ProcessResourceId implements Comparable<ProcessResourceId>, Serializable {

    @Serial
    private static final long serialVersionUID = 5075333115382283359L;
    private static final String CHAR_ARRAY = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final BigInteger CHAR_ARRAY_LENGTH = BigInteger.valueOf(CHAR_ARRAY.length());
    public static final String ID_SEPARATOR = "-";
    public static final String NULL_SHORT_ID_VALUE = "NULL"; // TODO maybe value "TOTOK" should be more accurate

    private ObjectId objectId;
    private String shortProcessId;

    public ProcessResourceId() {
        this.objectId = new ObjectId();
        this.shortProcessId = NULL_SHORT_ID_VALUE;
    }

    public ProcessResourceId(ObjectId processId) {
        this.objectId = new ObjectId();
        this.shortProcessId = generateShortProcessId(processId.toString());
    }

    public ProcessResourceId(String processId, String objectId) {
        this.objectId = new ObjectId(objectId);
        this.shortProcessId = generateShortProcessId(processId);
    }

    public ProcessResourceId(String processId, ObjectId objectId) {
        this.objectId = objectId;
        this.shortProcessId = generateShortProcessId(processId);
    }

    public ProcessResourceId(String compositeId) {
        String[] parts = compositeId.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid composite ID format: " + compositeId);
        }
        this.shortProcessId = parts[0];
        this.objectId = new ObjectId(parts[1]);
    }

    public String getFullId() {
        return shortProcessId + ID_SEPARATOR + objectId.toHexString();
    }

    public String getStringId() {
        return this.getFullId();
    }

    public Date getDate() {
        return objectId.getDate();
    }

    public int getTimestamp() {
        return objectId.getTimestamp();
    }

    @Override
    public String toString() {
        return getFullId();
    }

    private static String generateShortProcessId(String processId) {
        if (processId == null || processId.isEmpty()) {
            return null;
        }
        try {
            BigInteger number = new BigInteger(processId, 16);
            StringBuilder shortIdBuilder = new StringBuilder();

            while (number.compareTo(BigInteger.ZERO) > 0) {
                int remainder = number.mod(CHAR_ARRAY_LENGTH).intValue();
                shortIdBuilder.append(CHAR_ARRAY.charAt(remainder));
                number = number.divide(CHAR_ARRAY_LENGTH);
            }

            return shortIdBuilder.reverse().toString();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid input string for encoding: " + processId, e);
        }
    }

    public static String decodeShortProcessId(String shortProcessId) {
        if (shortProcessId == null || shortProcessId.isEmpty()) {
            return null;
        }
        BigInteger number = BigInteger.ZERO;
        for (char c : shortProcessId.toCharArray()) {
            number = number.multiply(CHAR_ARRAY_LENGTH);
            int index = CHAR_ARRAY.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("Invalid character in short process ID: " + c);
            }
            number = number.add(BigInteger.valueOf(index));
        }
        return number.toString(16);
    }

    @Override
    public int compareTo(ProcessResourceId other) {
        return this.getFullId().compareTo(other.getFullId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessResourceId that = (ProcessResourceId) o;
        return Objects.equals(objectId, that.objectId) && Objects.equals(shortProcessId, that.shortProcessId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, shortProcessId);
    }

}
