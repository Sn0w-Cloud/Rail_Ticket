package org.zerock.project.Service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.project.Entity.Board;
import org.zerock.project.Entity.Member_info;
import org.zerock.project.Repository.BoardRepository;
import org.zerock.project.Repository.Member_infoRepository;
import org.zerock.project.Entity.enums.BoardType;

import java.io.File;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final Member_infoRepository memberInfoRepository;

    // ✅ application.properties 값 주입
    @Value("${file.upload.board}")
    private String uploadDir;

    // ⭐ 게시판 타입별 조회
    public List<Board> findByType(BoardType type) {
        return boardRepository.findByBoardType(type);
    }

    // ⭐ 제목 검색
    public List<Board> searchByTitle(String keyword) {
        return boardRepository.findByTitleContaining(keyword);
    }

    // ⭐ 전체 조회
    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    public void write(
            String userId,
            BoardType boardType,
            String title,
            String content,
            MultipartFile imageFile
    ) {

        Member_info member = memberInfoRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("회원 정보 없음"));

        Board board = new Board();
        board.setBoardType(boardType);
        board.setTitle(title);
        board.setContent(content);
        board.setWriter(member);

        if (imageFile != null && !imageFile.isEmpty()) {

            String fileName = imageFile.getOriginalFilename();
            if (fileName == null) {
                throw new IllegalStateException("파일 이름이 없습니다");
            }

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    throw new IllegalStateException("업로드 디렉터리 생성 실패: " + uploadDir);
                }
            }

            try {
                File saveFile = new File(uploadDir + fileName);
                imageFile.transferTo(saveFile);
            } catch (Exception e) {
                throw new IllegalStateException("파일 업로드 실패", e);
            }

            board.setImageUrl("C:/Users/JUN/Desktop/JAVA/imge/" + fileName);
        }

        boardRepository.save(board);
    }

    @Transactional
    public void edit(
            Integer id,
            String loginUser,
            BoardType boardType,
            String title,
            String content,
            MultipartFile imageFile
    ) {

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        if (!board.getWriter().getUserId().equals(loginUser)) {
            throw new IllegalStateException("수정 권한 없음");
        }

        board.setTitle(title);
        board.setContent(content);
        board.setBoardType(boardType);

        if (imageFile != null && !imageFile.isEmpty()) {

            String fileName = imageFile.getOriginalFilename();

            File saveFile = new File(uploadDir + fileName);
            try {
                imageFile.transferTo(saveFile);
            } catch (Exception e) {
                throw new IllegalStateException("이미지 수정 실패", e);
            }

            board.setImageUrl("C:/Users/JUN/Desktop/JAVA/imge/" + fileName);
        }

        boardRepository.save(board);
    }


}
