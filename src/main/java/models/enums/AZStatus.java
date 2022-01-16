package models.enums;

public enum AZStatus {
    UP {
        public String toString() {
            return "Up";
        }
    },
    DOWN {
        public String toString() {
            return "Down";
        }
    };
}
