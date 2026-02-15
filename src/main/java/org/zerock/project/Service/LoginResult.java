package org.zerock.project.Service;

public enum LoginResult {
    SUCCESS,       // 로그인 성공
    ID_NOT_FOUND,  // 아이디 없음
    PASSWORD_INCORRECT, // 비밀번호 틀림
    DELETED
}
