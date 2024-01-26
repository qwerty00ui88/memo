package com.memo.post.bo;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.memo.common.FileManagerService;
import com.memo.post.domain.Post;
import com.memo.post.mapper.PostMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PostBO {
	// private Logger logger = LoggerFactory.getLogger(PostBO.class);
	// private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private PostMapper postMapper;

	@Autowired
	private FileManagerService fileManagerService;
	
	// 페이지 필드
	private static final int POST_MAX_SIZE = 3;
	
	public List<Post> getPostListByUserId(int userId, Integer prevId, Integer nextId) {
		// 게시글 번호 10 9 8 | 7 6 5 | 4 3 2 | 1
		// 만약 4 3 2 페이지에 있을 때
		// 1) 다음: 2보다 작은 3개 DESC
		// 2) 이전: 4보다 큰 3개 ASC(5 6 7) => List reverse (7 6 5)
		// 3) 페이징 정보 없음: 최신순 3개 DESC
		
		Integer standardId = null; // 기준이 되는 postId
		String direction = null; // 방향
		if(prevId != null) { // 2) 이전
			standardId = prevId;
			direction = "prev";
			
			List<Post> postList = postMapper.selectPostListByUserId(userId, standardId, direction, POST_MAX_SIZE);
			
			// reverse list 5 6 7 => 7 6 5
			Collections.reverse(postList); // 뒤집고 저장
			
			return postList;
		} else if(nextId != null) { // 1) 다음
			standardId = nextId;
			direction = "next";
		}
		
		// 3) 페이징 정보 없음, 1) 다음
		return postMapper.selectPostListByUserId(userId, standardId, direction, POST_MAX_SIZE);
	}
	
	// 이전 페이지의 마지막인가?
	public boolean isPrevLastPageByUserId(int userId, int prevId) {
		int postId = postMapper.selectPostIdByUseridSort(userId, "DESC");
		return postId == prevId; // 같으면 마지막이다.
	}
	
	// 다음 페이지의 마지막인가?
	public boolean isNextLastPageByUserId(int userId, int nextId) {
		int postId = postMapper.selectPostIdByUseridSort(userId, "ASC");
		return postId == nextId; // 같으면 마지막이다.
	}
	
	// input: params   output: X
	public void addPost(
			int userId, 
			String userLoginId,
			String subject,  
			String content,
			MultipartFile file) {
		
		String imagePath = null;
		
		// 업로드할 이미지가 있을 때 업로드
		if(file != null) {
			imagePath = fileManagerService.saveFile(userLoginId, file);
		}
		
		postMapper.insertPost(userId, subject, content, imagePath);
	}
	
	// input: 글번호, userId   output: Post
	public Post getPostByPostIdUserId(int postId, int userId) {
		return postMapper.selectPostByPostIdUserId(postId, userId);
	}
	
	public void updatePostById(
			int userId, 
			String userLoginId,
			int postId,
			String subject,  
			String content,
			MultipartFile file) {
		
		// 기존글을 가져온다.(1. 이미지 교체시 삭제하기 위해, 2. 업데이트 대상이 있는지 확인)
		// 실무에서는 캐시에서 select
		Post post = postMapper.selectPostByPostIdUserId(postId, userId);
		if(post == null) {
			log.info("[글 수정] post is null. postId:{}, userId:{}", postId, userId);
			return;
		}		
		
		// 파일이 있다면
		// 1) 새 이미지를 업로드 한다.
		// 2) 1번 단계가 성공하면 기존 이미지 제거(기존 이미지가 있다면)
		String imagePath = null;
		if(file != null) {
			// 업로드
			imagePath = fileManagerService.saveFile(userLoginId, file);
			
			// 업로드 성공 시 기존 이미지 제거
			if(imagePath != null && post.getImagePath() != null) {
				// 업로드 성공하고 기존 이미지 있으면 서버의 파일 제거
				fileManagerService.deleteFile(post.getImagePath());
			}
		}
		
		// db update
		postMapper.updatePostByPostId(postId, subject, content, imagePath);
	}
	
	public void deletePostByPostIdUserId(int postId, int userId) {
		Post post = postMapper.selectPostByPostIdUserId(postId, userId);
		
		// 기존 글이 있는지 확인
		if(post == null) {
			log.info("[글 삭제] post is null. postId:{}, userId:{}", postId, userId);
			return;
		}
		
		// DB 삭제
		int deleteRowCount = postMapper.deletePostByPostId(postId);
		
		// DB 삭제 성공 && 이미지가 존재하면 => 이미지 삭제
		if(deleteRowCount > 0 && post.getImagePath() != null) {
			fileManagerService.deleteFile(post.getImagePath());
		}
	}
	
}
