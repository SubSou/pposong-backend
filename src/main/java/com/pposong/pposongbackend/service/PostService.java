
package com.pposong.pposongbackend.service;

import com.pposong.pposongbackend.dto.post.CreatePostRequest;
import com.pposong.pposongbackend.dto.post.PostResponse;
import com.pposong.pposongbackend.dto.post.UpdatePostRequest;

import com.pposong.pposongbackend.entity.Post;
import com.pposong.pposongbackend.entity.User;
import com.pposong.pposongbackend.entity.PostImage;
import com.pposong.pposongbackend.entity.Comment;

import com.pposong.pposongbackend.repository.CommentRepository;
import com.pposong.pposongbackend.repository.LikeRepository;
import com.pposong.pposongbackend.repository.PostImageRepository;
import com.pposong.pposongbackend.repository.PostRepository;
import com.pposong.pposongbackend.repository.UserRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostImageRepository postImageRepository;
    private final S3Service s3Service;
    private final JdbcTemplate jdbcTemplate;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            PostImageRepository postImageRepository,
            S3Service s3Service,
            JdbcTemplate jdbcTemplate
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.postImageRepository = postImageRepository;
        this.s3Service = s3Service;
        this.jdbcTemplate = jdbcTemplate;
    }

    /*
     * JDBC 시간 확인 (임시 테스트)
     */
    private void checkCreatedAt() {

        Long postId = 3L;

        LocalDateTime jdbcTime = jdbcTemplate.queryForObject(
                "SELECT created_at FROM posts WHERE id = ?",
                (rs, rowNum) ->
                        rs.getObject("created_at", LocalDateTime.class),
                postId
        );

        Timestamp timestamp = jdbcTemplate.queryForObject(
                "SELECT created_at FROM posts WHERE id = ?",
                (rs, rowNum) ->
                        rs.getTimestamp("created_at"),
                postId
        );

        String rawTime = jdbcTemplate.queryForObject(
                "SELECT CAST(created_at AS CHAR) FROM posts WHERE id = ?",
                String.class,
                postId
        );

        System.out.println("===== JDBC 시간 확인 =====");
        System.out.println("게시글 ID: " + postId);
        System.out.println("MySQL 문자열: " + rawTime);
        System.out.println("JDBC LocalDateTime: " + jdbcTime);
        System.out.println("JDBC Timestamp: " + timestamp);
        System.out.println("JVM 시간대: " + java.time.ZoneId.systemDefault());
    }

    /*
     * 게시글 작성
     */
    @Transactional
    public void createPost(
            Long userId,
            CreatePostRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        Post post = new Post(
                request.getContent(),
                user
        );

        Post savedPost = postRepository.save(post);

        if (request.getImageUrls() != null) {

            List<String> imageUrls = request.getImageUrls();

            if (imageUrls.size() > 10) {
                throw new IllegalArgumentException(
                        "이미지는 최대 10장까지 등록할 수 있습니다."
                );
            }

            for (int i = 0; i < imageUrls.size(); i++) {

                PostImage postImage = new PostImage(
                        savedPost,
                        imageUrls.get(i),
                        i
                );

                postImageRepository.save(postImage);
            }
        }
    }

    /*
     * 게시글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getPosts(Long userId) {

        System.out.println("===== getPosts 실행 확인 =====");

        // JDBC 시간 확인
        checkCreatedAt();

        List<Post> posts = postRepository
                .findAllByActiveOrderByCreatedAtDesc("Y");

        return posts.stream()
                .map(post -> {

                    long likeCount =
                            likeRepository.countByPostId(
                                    post.getId()
                            );

                    boolean liked =
                            likeRepository
                                    .findByPostIdAndUserId(
                                            post.getId(),
                                            userId
                                    )
                                    .isPresent();

                    long commentCount =
                            commentRepository
                                    .countByPostIdAndActive(
                                            post.getId(),
                                            "Y"
                                    );

                    List<String> imageUrls =
                            postImageRepository
                                    .findAllByPostIdOrderByImageOrderAsc(
                                            post.getId()
                                    )
                                    .stream()
                                    .map(PostImage::getImageUrl)
                                    .toList();

                    if (post.getId().equals(3L)) {
                        System.out.println(
                                "Hibernate createdAt: "
                                        + post.getCreatedAt()
                        );
                    }

                    return new PostResponse(
                            post,
                            likeCount,
                            liked,
                            commentCount,
                            imageUrls
                    );
                })
                .toList();
    }

    /*
     * 게시글 삭제
     */
    @Transactional
    public void deletePost(
            Long userId,
            Long postId
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "게시글을 찾을 수 없습니다."
                        )
                );

        if ("N".equals(post.getActive())) {
            throw new IllegalStateException(
                    "이미 삭제된 게시글입니다."
            );
        }

        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalStateException(
                    "게시글을 삭제할 권한이 없습니다."
            );
        }

        List<PostImage> postImages =
                postImageRepository
                        .findAllByPostIdOrderByImageOrderAsc(
                                postId
                        );

        for (PostImage postImage : postImages) {
            s3Service.deleteImage(
                    postImage.getImageUrl()
            );
        }

        postImageRepository.deleteAllByPostId(
                postId
        );

        List<Comment> comments =
                commentRepository
                        .findAllByPostIdAndActiveOrderByCreatedAtAsc(
                                postId,
                                "Y"
                        );

        for (Comment comment : comments) {
            comment.delete();
        }

        likeRepository.deleteAllByPostId(
                postId
        );

        post.delete();
    }

    /*
     * 게시글 수정
     */
    @Transactional
    public PostResponse updatePost(
            Long userId,
            Long postId,
            UpdatePostRequest request
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "게시글을 찾을 수 없습니다."
                        )
                );

        if ("N".equals(post.getActive())) {
            throw new IllegalStateException(
                    "삭제된 게시글은 수정할 수 없습니다."
            );
        }

        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalStateException(
                    "게시글을 수정할 권한이 없습니다."
            );
        }

        post.updateContent(
                request.getContent()
        );

        List<String> oldImageUrls =
                postImageRepository
                        .findAllByPostIdOrderByImageOrderAsc(
                                postId
                        )
                        .stream()
                        .map(PostImage::getImageUrl)
                        .toList();

        List<String> imageUrls =
                request.getImageUrls();

        if (imageUrls != null) {

            if (imageUrls.size() > 10) {
                throw new IllegalArgumentException(
                        "이미지는 최대 10장까지 등록할 수 있습니다."
                );
            }

            List<String> removedImageUrls =
                    oldImageUrls.stream()
                            .filter(oldUrl ->
                                    !imageUrls.contains(oldUrl)
                            )
                            .toList();

            postImageRepository.deleteAllByPostId(
                    postId
            );

            for (int i = 0; i < imageUrls.size(); i++) {

                PostImage postImage = new PostImage(
                        post,
                        imageUrls.get(i),
                        i
                );

                postImageRepository.save(
                        postImage
                );
            }

            for (String removedImageUrl : removedImageUrls) {
                s3Service.deleteImage(
                        removedImageUrl
                );
            }
        }

        long likeCount =
                likeRepository.countByPostId(
                        postId
                );

        boolean liked =
                likeRepository
                        .findByPostIdAndUserId(
                                postId,
                                userId
                        )
                        .isPresent();

        long commentCount =
                commentRepository
                        .countByPostIdAndActive(
                                postId,
                                "Y"
                        );

        List<String> updatedImageUrls =
                postImageRepository
                        .findAllByPostIdOrderByImageOrderAsc(
                                postId
                        )
                        .stream()
                        .map(PostImage::getImageUrl)
                        .toList();

        return new PostResponse(
                post,
                likeCount,
                liked,
                commentCount,
                updatedImageUrls
        );
    }

    /*
     * 게시글 상세 조회
     */
    @Transactional(readOnly = true)
    public PostResponse getPost(
            Long userId,
            Long postId
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "게시글을 찾을 수 없습니다."
                        )
                );

        if ("N".equals(post.getActive())) {
            throw new IllegalStateException(
                    "삭제된 게시글입니다."
            );
        }

        long likeCount =
                likeRepository.countByPostId(
                        postId
                );

        boolean liked =
                likeRepository
                        .findByPostIdAndUserId(
                                postId,
                                userId
                        )
                        .isPresent();

        long commentCount =
                commentRepository
                        .countByPostIdAndActive(
                                postId,
                                "Y"
                        );

        List<String> imageUrls =
                postImageRepository
                        .findAllByPostIdOrderByImageOrderAsc(
                                postId
                        )
                        .stream()
                        .map(PostImage::getImageUrl)
                        .toList();

        return new PostResponse(
                post,
                likeCount,
                liked,
                commentCount,
                imageUrls
        );
    }

    /*
     * 내가 작성한 게시글 조회
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getMyPosts(
            Long userId
    ) {
        List<Post> posts =
                postRepository
                        .findAllByUserIdAndActiveOrderByCreatedAtDesc(
                                userId,
                                "Y"
                        );

        return posts.stream()
                .map(post -> {

                    long likeCount =
                            likeRepository.countByPostId(
                                    post.getId()
                            );

                    boolean liked =
                            likeRepository
                                    .findByPostIdAndUserId(
                                            post.getId(),
                                            userId
                                    )
                                    .isPresent();

                    long commentCount =
                            commentRepository
                                    .countByPostIdAndActive(
                                            post.getId(),
                                            "Y"
                                    );

                    List<String> imageUrls =
                            postImageRepository
                                    .findAllByPostIdOrderByImageOrderAsc(
                                            post.getId()
                                    )
                                    .stream()
                                    .map(PostImage::getImageUrl)
                                    .toList();

                    return new PostResponse(
                            post,
                            likeCount,
                            liked,
                            commentCount,
                            imageUrls
                    );
                })
                .toList();
    }

    /*
     * 좋아요한 게시글 조회
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getLikedPosts(
            Long userId
    ) {
        List<Post> posts =
                likeRepository.findLikedPostsByUserId(
                        userId
                );

        return posts.stream()
                .map(post -> {

                    long likeCount =
                            likeRepository.countByPostId(
                                    post.getId()
                            );

                    boolean liked =
                            likeRepository
                                    .findByPostIdAndUserId(
                                            post.getId(),
                                            userId
                                    )
                                    .isPresent();

                    long commentCount =
                            commentRepository
                                    .countByPostIdAndActive(
                                            post.getId(),
                                            "Y"
                                    );

                    List<String> imageUrls =
                            postImageRepository
                                    .findAllByPostIdOrderByImageOrderAsc(
                                            post.getId()
                                    )
                                    .stream()
                                    .map(PostImage::getImageUrl)
                                    .toList();

                    return new PostResponse(
                            post,
                            likeCount,
                            liked,
                            commentCount,
                            imageUrls
                    );
                })
                .toList();
    }
}
