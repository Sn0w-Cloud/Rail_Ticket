package org.zerock.project.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zerock.project.Entity.Board;
import org.zerock.project.Entity.enums.BoardType;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Integer> {

    List<Board> findByBoardType(BoardType boardType);

    List<Board> findByTitleContaining(String keyword);
}

