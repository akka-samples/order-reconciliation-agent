package com.akka.application.model;

public record ChainOfThought(
        String Thought, String Tool, boolean PAUSE, String input, String Observation,
        String Answer) {
}
