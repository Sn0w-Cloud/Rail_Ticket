/*
테이블


회원정보			열차			구매 목록		장바구니


아이디			열차별번호		번호		번호
비번			출바시간			아이디		아이디
이름			도착시간			이름		좌석번호
주소			출발역			가격		가격
휴대폰 번호		도착역			열차번호		열차번호
회원타입			가격			좌석번호
이메일
생일
가입날짜

할인 *특정 시간, 구독권		게시판		답글		좌석


번호				        번호		번호		열차번호
할인율				        제목		아이디		좌석ID
할인 시작일			        내용		내용		상태
할인 종료일			        아이디		이미지		좌석번호
이미지				        작성날짜		작성날짜
게시판 타입			        이미지		수정날짜
				            조회수		가시판아이디
				            게시판 타입
				            업데이트 날짜
				            소프트웨어삭제 여부

create table Member_info (
    user_id varchar(15) primary key ,
    user_pw       VARCHAR(100)            not null,
    user_name     varchar(50)            not null,
    user_address   varchar(100)           not null,
    user_email    varchar(100)           not null,
    phone_number   varchar(15)            not null,
    member_type    enum ('user','admin') not NULL,
    birth_date date,
    created_at timestamp default current_timestamp
);

create table Train_info (
    trn_no int unique not null ,
    arrplacecode varchar(10) not null ,
    arrplacename varchar(50) not null ,
    depplacecode varchar(10) not null ,
    depplacename varchar(50) not null ,
    arrplandtime datetime,
    depplandtime datetime,
    adultcharge int,
    trn_id int AUTO_INCREMENT primary key
);

create table Cart (
    cart_id int AUTO_INCREMENT PRIMARY KEY,
    user_id varchar(15) not null ,
    CONSTRAINT fk_cart_member foreign key (user_id) references Member_info(user_id),
    total_price int not null ,
    seat_id varchar(10) not null ,
    CONSTRAINT fk_cart_trn FOREIGN KEY (seat_id) REFERENCES seats(seat_id),
    trn_id int not null ,
    CONSTRAINT fk_cart_seat FOREIGN KEY (trn_id) REFERENCES Train_Info(trn_id)
);

create table Purchase_list (
                               list_id int AUTO_INCREMENT PRIMARY KEY,
                               user_id varchar(15) not null ,
                               CONSTRAINT fk_purchase_member foreign key (user_id) references Member_info(user_id),
                               list_name varchar(500) not null ,
                               price int not null ,
                               trn_id int not null ,
                               CONSTRAINT fk_purchase_trn FOREIGN KEY (trn_id) REFERENCES Train_Info(trn_id),
                               seat_id varchar(10) not null,
                               CONSTRAINT fk_purchase_seat FOREIGN KEY (seat_id) REFERENCES seats(seat_id)
);

CREATE TABLE board (
                       board_id INT AUTO_INCREMENT PRIMARY KEY,
                       board_type VARCHAR(20) NOT NULL,
                       title VARCHAR(255) NOT NULL,
                       content TEXT NOT NULL,
                       writer_id varchar(15) NOT NULL,
                       view_count INT DEFAULT 0,
                       image_url VARCHAR(500),  -- 이미지 경로 저장
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       CONSTRAINT fk_board_writer FOREIGN KEY (writer_id) REFERENCES Member_info(user_id)
);

CREATE TABLE board_reply (
                             reply_id INT AUTO_INCREMENT PRIMARY KEY,       -- 답변 고유 ID
                             board_id INT NOT NULL,                         -- 게시글 ID (board.board_id FK)
                             admin_id varchar(15) NOT NULL,                         -- 답변 작성자 ID (관리자)
                             content TEXT NOT NULL,                             -- 답변 내용
                             image_url VARCHAR(500),                            -- 답변 첨부 이미지 URL
                             created_at DATETIME DEFAULT CURRENT_TIMESTAMP,    -- 작성일
                             updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 수정일

                             CONSTRAINT fk_reply_board FOREIGN KEY (board_id) REFERENCES board(board_id) ON DELETE CASCADE,
                             CONSTRAINT fk_reply_admin FOREIGN KEY (admin_id) REFERENCES Member_info(user_id)
);




CREATE TABLE events (
                        event_id INT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,                -- 이벤트 이름
                        event_type VARCHAR(20) NOT NULL,           -- 이벤트 종류: MORNING / SUBSCRIPTION / SPECIAL
                        discount_value DECIMAL(5,2) NOT NULL      -- 할인 값
);

CREATE TABLE seats
(
    seat_id     INT AUTO_INCREMENT PRIMARY KEY,  -- 좌석 ID
    trn_id INT        NOT NULL,             -- 열차 스케줄 ID
    seat_number VARCHAR(5) NOT NULL,             -- 좌석 번호
    status      VARCHAR(15) DEFAULT 'AVAILABLE', -- 좌석 상태: AVAILABLE / RESERVED / BOOKED
    reserved_at DATETIME    DEFAULT NULL,        -- RESERVED 시작 시간
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_seat_id FOREIGN KEY (trn_id) REFERENCES Train_info(trn_id)
);

insert into webdb.Member_info
(user_id, user_pw, user_name, user_address, user_email, phone_number, member_type, birth_date)
values ('test', '1235', '홍길동', '부산', 'test@email.com', '01012345678',
        'user', current_date);

 */








