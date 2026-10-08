package com.pposong.pposongbackend.service;

import com.pposong.pposongbackend.dto.post.CreatePostRequest;
import com.pposong.pposongbackend.dto.post.PostResponse;
import com.pposong.pposongbackend.dto.post.UpdatePostRequest;

import com.pposong.pposongbackend.entity.Post;
import com.pposong.pposongbackend.entity.User;

import com.pposong.pposongbackend.repository.CommentRepository;
import com.pposong.pposongbackend.repository.LikeRepository;
import com.pposong.pposongbackend.repository.PostImageRepository;
import com.pposong.pposongbackend.repository.PostRepository;
import com.pposong.pposongbackend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.pposong.pposongbackend.entity.PostImage;

import com.pposong.pposongbackend.entity.Comment;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PostImageRepository postImageRepository;
    private final S3Service s3Service;

    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            LikeRepository likeRepository,
            CommentRepository commentRepository,
            PostImageRepository postImageRepository,
            S3Service s3Service
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.postImageRepository = postImageRepository;
        this.s3Service = s3Service;
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

        Post savedPost =
                postRepository.save(post);

        if (request.getImageUrls() != null) {

            List<String> imageUrls =
                    request.getImageUrls();

            if (imageUrls.size() > 10) {
                throw new IllegalArgumentException(
                        "이미지는 최대 10장까지 등록할 수 있습니다."
                );
            }

            for (int i = 0; i < imageUrls.size(); i++) {

                PostImage postImage =
                        new PostImage(
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

        List<Post> posts =
                postRepository
                        .findAllByActiveOrderByCreatedAtDesc("Y");

        return posts.stream()
                .map(post -> {

                    // 좋아요 개수
                    long likeCount =
                            likeRepository.countByPostId(
                                    post.getId()
                            );

                    // 내가 좋아요를 눌렀는지
                    boolean liked =
                            likeRepository
                                    .findByPostIdAndUserId(
                                            post.getId(),
                                            userId
                                    )
                                    .isPresent();

                    // 댓글 개수
                    long commentCount =
                            commentRepository
                                    .countByPostIdAndActive(
                                            post.getId(),
                                            "Y"
                                    );

                    // 게시글 이미지 URL 목록
                    List<String> imageUrls =
                            postImageRepository
                                    .findAllByPostIdOrderByImageOrderAsc(
                                            post.getId()
                                    )
                                    .stream()
                                    .map(postImage ->
                                            postImage.getImageUrl()
                                    )
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

        // 이미 삭제된 게시글인지 확인
        if ("N".equals(post.getActive())) {
            throw new IllegalStateException(
                    "이미 삭제된 게시글입니다."
            );
        }

        // 작성자 확인
        if (!post.getUser()
                .getId()
                .equals(userId)) {

            throw new IllegalStateException(
                    "게시글을 삭제할 권한이 없습니다."
            );
        }

        // 게시글에 등록된 이미지 조회
        List<PostImage> postImages =
                postImageRepository
                        .findAllByPostIdOrderByImageOrderAsc(
                                postId
                        );

        // S3 이미지 삭제
        for (PostImage postImage : postImages) {
            s3Service.deleteImage(
                    postImage.getImageUrl()
            );
        }

        // post_images DB 데이터 삭제
        postImageRepository.deleteAllByPostId(
                postId
        );

        // 게시글에 달린 댓글 조회
        List<Comment> comments =
                commentRepository
                        .findAllByPostIdAndActiveOrderByCreatedAtAsc(
                                postId,
                                "Y"
                        );

        // 댓글 소프트 삭제
        for (Comment comment : comments) {
            comment.delete();
        }

        // 좋아요 데이터 삭제
        likeRepository.deleteAllByPostId(
                postId
        );

        // 게시글 소프트 삭제
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
        Post post = postRepository
                .findById(postId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "게시글을 찾을 수 없습니다."
                        )
                );

        // 삭제된 게시글인지 확인
        if ("N".equals(post.getActive())) {
            throw new IllegalStateException(
                    "삭제된 게시글은 수정할 수 없습니다."
            );
        }

        // 본인이 작성한 게시글인지 확인
        if (!post.getUser()
                .getId()
                .equals(userId)) {
            throw new IllegalStateException(
                    "게시글을 수정할 권한이 없습니다."
            );
        }

        // 게시글 내용 수정
        post.updateContent(
                request.getContent()
        );

// 수정 전 기존 이미지 URL 가져오기
        List<String> oldImageUrls =
                postImageRepository
                        .findAllByPostIdOrderByImageOrderAsc(
                                postId
                        )
                        .stream()
                        .map(PostImage::getImageUrl)
                        .toList();

// 수정 후 이미지 목록
        List<String> imageUrls =
                request.getImageUrls();

        if (imageUrls != null) {

            if (imageUrls.size() > 10) {
                throw new IllegalArgumentException(
                        "이미지는 최대 10장까지 등록할 수 있습니다."
                );
            }

            // 기존 이미지 중 사용자가 삭제한 이미지 찾기
            List<String> removedImageUrls =
                    oldImageUrls.stream()
                            .filter(oldUrl ->
                                    !imageUrls.contains(oldUrl)
                            )
                            .toList();

            // DB의 기존 이미지 정보 삭제
            postImageRepository.deleteAllByPostId(
                    postId
            );

            // 수정 후 이미지 목록 다시 저장
            for (int i = 0; i < imageUrls.size(); i++) {

                PostImage postImage =
                        new PostImage(
                                post,
                                imageUrls.get(i),
                                i
                        );

                postImageRepository.save(
                        postImage
                );
            }

            // 사용자가 제거한 이미지를 S3에서도 삭제
            for (String removedImageUrl : removedImageUrls) {
                s3Service.deleteImage(
                        removedImageUrl
                );
            }
        }

        /*
         * 수정된 게시글 Response
         */

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

        // 삭제된 게시글인지 확인
        if ("N".equals(post.getActive())) {
            throw new IllegalStateException(
                    "삭제된 게시글입니다."
            );
        }

        // 좋아요 개수
        long likeCount =
                likeRepository.countByPostId(
                        postId
                );

        // 현재 사용자가 좋아요를 눌렀는지
        boolean liked =
                likeRepository
                        .findByPostIdAndUserId(
                                postId,
                                userId
                        )
                        .isPresent();

        // 댓글 개수
        long commentCount =
                commentRepository
                        .countByPostIdAndActive(
                                postId,
                                "Y"
                        );

        // 게시글 이미지 URL 목록
        List<String> imageUrls =
                postImageRepository
                        .findAllByPostIdOrderByImageOrderAsc(
                                postId
                        )
                        .stream()
                        .map(postImage ->
                                postImage.getImageUrl()
                        )
                        .toList();

        return new PostResponse(
                post,
                likeCount,
                liked,
                commentCount,
                imageUrls
        );
    }

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