package org.zerock.project.Entity.enums;

public enum BoardType {
    NOTICE("공지사항"),
    QNA("건의 게시판"),
    EVENT("이벤트");

    private final String label;

    BoardType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
