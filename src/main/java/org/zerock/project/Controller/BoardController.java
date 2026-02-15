package org.zerock.project.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.project.Entity.Board;
import org.zerock.project.Entity.Member_info;
import org.zerock.project.Entity.enums.BoardType;
import org.zerock.project.Repository.BoardRepository;
import org.zerock.project.Repository.Member_infoRepository;
import org.zerock.project.Service.BoardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final BoardRepository boardRepository;
    private final Member_infoRepository memberInfoRepository;

    /* =========================
       게시판 목록 화면
       ========================= */
    @GetMapping
    public String list(
            @RequestParam(required = false) BoardType type,
            Model model
    ) {

        List<Board> boardList;

        if (type != null) {
            boardList = boardService.findByType(type);
        } else {
            boardList = boardService.findAll();
        }

        model.addAttribute("boardList", boardList);
        return "board";
    }


    /* =========================
       게시글 상세 화면
       ========================= */
    @GetMapping("/read/{id}")
    public String boardRead(
            @PathVariable Integer id,
            Model model
    ) {

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        model.addAttribute("board", board);

        if (board.getImageUrl() != null) {
            String fullPath = board.getImageUrl();
            String fileName = new File(fullPath).getName();
            String imageUrl = "/images/" + fileName;
            model.addAttribute("imageUrl", imageUrl);
        }

        return "View_Board";
    }

    @GetMapping("/write")
    public String boardWrite(Model model, HttpSession session) {

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/member/login";
        }

        String userId = loginUser.toString();

        Member_info member = memberInfoRepository
                .findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("회원 정보 없음"));

        Board board = new Board();
        board.setWriter(member);

        model.addAttribute("board", board);

        return "board_write";
    }

    @PostMapping("/write")
    public String write(
            @RequestParam BoardType boardType,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile imageFile,
            HttpSession session
    ) throws Exception {

        String loginUser = (String) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/member/login";
        }

        boardService.write(
                loginUser,
                boardType,
                title,
                content,
                imageFile
        );

        return "redirect:/board";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id,
                           HttpSession session,
                           Model model) {

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        String loginUser = (String) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/member/login";
        }

        if (!board.getWriter().getUserId().equals(loginUser)) {
            return "redirect:/board";
        }

        model.addAttribute("board", board);
        return "board_edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(
            @PathVariable Integer id,
            @RequestParam BoardType boardType,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile imageFile,

            HttpSession session
    ) {

        String loginUser = (String) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/member/login";
        }

        boardService.edit(
                id,
                loginUser,
                boardType,
                title,
                content,
                imageFile
        );

        return "redirect:/board/read/" + id;
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id,
                         HttpSession session) {

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        String loginUser = (String) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/member/login";
        }

        if (!board.getWriter().getUserId().equals(loginUser)) {
            return "redirect:/board";
        }

        boardRepository.delete(board);

        return "redirect:/board";
    }

    @Value("${file.upload.board}")
    private String uploadDir;

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Integer id) throws Exception {

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        if (board.getImageUrl() == null) {
            throw new IllegalArgumentException("첨부파일 없음");
        }

        String fileName = board.getImageUrl().replace("/Users/JUN/Desktop/JAVA/imge/", "");
        Path filePath = Paths.get(uploadDir + fileName);

        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

}