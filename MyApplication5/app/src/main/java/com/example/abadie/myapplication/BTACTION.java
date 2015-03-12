package com.example.abadie.myapplication;

public enum BTACTION{
    ACTION_FOUND {
        public String toString() {
            return "ACTION_FOUND";
        }
    },

    ACTION_DISCOVERY_STARTED {
        public String toString() {
            return "ACTION_DISCOVERY_STARTED";
        }
    },

    ACTION_DISCOVERY_FINISHED {
        public String toString() {
            return "ACTION_DISCOVERY_FINISHED";
        }
    }
}
