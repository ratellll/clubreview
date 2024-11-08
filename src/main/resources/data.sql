-- Club 데이터
INSERT INTO Club (name, location, description, call_number, average_rating) VALUES
                                                                                ('Octagon', '서울 강남구', '강남에 위치한 인기 클럽 Octagon', '010-1000-1001', 4.6),
                                                                                ('M2', '서울 마포구', '홍대의 클럽 M2', '010-1000-1002', 4.2),
                                                                                ('Arena', '서울 강남구', '강남에서 유명한 클럽 Arena', '010-1000-1003', 4.5),
                                                                                ('Cakeshop', '서울 용산구', '이태원 클럽 Cakeshop', '010-1000-1004', 4.1),
                                                                                ('Club Made', '서울 마포구', '홍대의 힙한 클럽 Made', '010-1000-1005', 4.3),
                                                                                ('NB2', '서울 마포구', '홍대의 유명한 힙합 클럽 NB2', '010-1000-1006', 4.4),
                                                                                ('FAUST', '서울 중구', '을지로에 위치한 클럽 FAUST', '010-1000-1007', 3.8),
                                                                                ('SOAP', '서울 용산구', '이태원의 클럽 SOAP', '010-1000-1008', 4.0),
                                                                                ('Modeci', '서울 강남구', '럭셔리 클럽 Modeci', '010-1000-1009', 4.2),
                                                                                ('Mass', '서울 강남구', '강남의 대형 클럽 Mass', '010-1000-1010', 4.0),
                                                                                ('Chroma', '서울 송파구', '잠실에 위치한 대형 클럽 Chroma', '010-1000-1011', 4.7),
                                                                                ('Move', '서울 강남구', '음악이 좋은 클럽 Move', '010-1000-1012', 3.9),
                                                                                ('The Henz Club', '서울 마포구', '힙합 분위기의 클럽 The Henz Club', '010-1000-1013', 4.1),
                                                                                ('Hidden Cellar', '서울 용산구', '이태원의 Hidden Cellar', '010-1000-1014', 3.7),
                                                                                ('Vurt', '서울 마포구', '홍대의 작은 클럽 Vurt', '010-1000-1015', 4.0),
                                                                                ('DStar', '서울 중구', '중구에 위치한 DStar', '010-1000-1016', 3.9),
                                                                                ('Avant Garde', '서울 강남구', '미니멀 분위기의 Avant Garde', '010-1000-1017', 4.4),
                                                                                ('Volume', '서울 강남구', '강남의 큰 클럽 Volume', '010-1000-1018', 4.3),
                                                                                ('Monkey Museum', '서울 강남구', '강남의 Monkey Museum', '010-1000-1019', 4.2),
                                                                                ('The A', '서울 송파구', '송파구의 클럽 The A', '010-1000-1020', 3.6),
                                                                                ('Flex', '서울 마포구', '힙합 음악이 좋은 Flex', '010-1000-1021', 4.1),
                                                                                ('Baobab', '서울 용산구', '이태원의 Baobab', '010-1000-1022', 4.0),
                                                                                ('Alfie', '서울 중구', '알피에서의 특별한 경험', '010-1000-1023', 3.8),
                                                                                ('Escobar', '서울 용산구', '이태원의 Escobar', '010-1000-1024', 4.2),
                                                                                ('Made Moiselle', '서울 강남구', '강남의 Made Moiselle', '010-1000-1025', 4.5),
                                                                                ('Club Odes', '서울 강남구', '강남에 위치한 Club Odes', '010-1000-1026', 3.9),
                                                                                ('Madholic', '서울 마포구', '홍대의 Madholic', '010-1000-1027', 4.3),
                                                                                ('Once in a Blue Moon', '서울 강남구', '재즈와 함께하는 특별한 경험', '010-1000-1028', 4.6),
                                                                                ('Odin', '서울 용산구', '이태원의 분위기 있는 클럽 Odin', '010-1000-1029', 4.1),
                                                                                ('The Function', '서울 중구', '모던한 클럽 The Function', '010-1000-1030', 4.0);

-- User 데이터 (테스트 계정 5개)
INSERT INTO User (username, password, role) VALUES ('testuser1', 'password1', 'USER');
INSERT INTO User (username, password, role) VALUES ('testuser2', 'password2', 'USER');
INSERT INTO User (username, password, role) VALUES ('testuser3', 'password3', 'USER');
INSERT INTO User (username, password, role) VALUES ('testuser4', 'password4', 'USER');
INSERT INTO User (username, password, role) VALUES ('testuser5', 'password5', 'USER');


-- Club ID 1에 대한 리뷰 데이터
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('좋은 경험이었습니다!', 5, 1, 1, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('음악이 좋아요!', 4, 1, 2, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('조금 혼잡했어요.', 3, 1, 3, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('분위기 최고!', 5, 1, 4, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('다시 오고 싶어요.', 4, 1, 5, CURRENT_TIMESTAMP);

-- Club ID 2에 대한 리뷰 데이터
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('즐거운 밤이었어요!', 4, 2, 1, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('추천합니다.', 5, 2, 2, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('조금 시끄러웠어요.', 3, 2, 3, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('서비스가 좋았어요.', 4, 2, 4, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('전반적으로 만족해요.', 5, 2, 5, CURRENT_TIMESTAMP);

-- Club ID 3에 대한 리뷰 데이터
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('굉장히 붐벼요.', 3, 3, 1, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('음악이 너무 좋아요.', 5, 3, 2, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('분위기가 좋아요.', 4, 3, 3, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('친절한 직원들이 많아요.', 4, 3, 4, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('특별한 경험이었어요.', 5, 3, 5, CURRENT_TIMESTAMP);

-- Club ID 4에 대한 리뷰 데이터
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('다시 가고 싶어요.', 5, 4, 1, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('음악이 마음에 들어요.', 4, 4, 2, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('조금 시끄러웠어요.', 3, 4, 3, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('추천하고 싶어요.', 4, 4, 4, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('전반적으로 만족스러웠어요.', 5, 4, 5, CURRENT_TIMESTAMP);

-- Club ID 5에 대한 리뷰 데이터
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('기대 이상이었어요!', 5, 5, 1, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('또 가고 싶어요!', 4, 5, 2, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('기분 좋은 밤이었어요.', 5, 5, 3, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('음악이 너무 커요.', 3, 5, 4, CURRENT_TIMESTAMP);
INSERT INTO Review (comment, rating, club_id, user_id, created_at) VALUES ('조용했으면 좋겠어요.', 2, 5, 5, CURRENT_TIMESTAMP);

