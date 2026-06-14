package com.tale.test.service;

import com.blade.exception.ValidatorException;
import com.tale.model.dto.Comment;
import com.tale.model.entity.Comments;
import com.tale.model.entity.Contents;
import com.tale.model.params.CommentParam;
import com.tale.service.CommentsService;
import com.tale.utils.TaleUtils;
import com.vdurmont.emoji.EmojiParser;
import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.core.AnimaQuery;
import io.github.biezhi.anima.core.ResultKey;
import io.github.biezhi.anima.core.dml.Delete;
import io.github.biezhi.anima.core.dml.Select;
import io.github.biezhi.anima.core.dml.Update;
import io.github.biezhi.anima.page.Page;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * CommentsService unit tests
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CommentsServiceTest {

    @InjectMocks
    private CommentsService commentsService;

    private MockedStatic<Anima> mockedStatic;
    private MockedStatic<TaleUtils> mockedTaleUtils;
    private MockedStatic<EmojiParser> mockedEmoji;
    private MockedConstruction<Comments> mockedCommentsConstruction;

    @BeforeEach
    void setUp() {
        mockedStatic = mockStatic(Anima.class);
        try {
            // Anima.select() -> Select -> AnimaQuery chain (RETURNS_SELF for fluent chains)
            Select mockSelect = mock(Select.class);
            AnimaQuery mockQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
            mockedStatic.when(Anima::select).thenReturn(mockSelect);
            lenient().when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);

            // Anima.delete() -> Delete -> AnimaQuery chain
            Delete mockDelete = mock(Delete.class);
            AnimaQuery mockDeleteQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
            mockedStatic.when(Anima::delete).thenReturn(mockDelete);
            lenient().when(mockDelete.from(any(Class.class))).thenReturn(mockDeleteQuery);

            // Anima.update() -> Update -> AnimaQuery chain
            Update mockUpdate = mock(Update.class);
            AnimaQuery mockUpdateQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
            mockedStatic.when(Anima::update).thenReturn(mockUpdate);
            lenient().when(mockUpdate.from(any(Class.class))).thenReturn(mockUpdateQuery);

            // Mock TaleUtils.cleanXSS to pass through
            mockedTaleUtils = mockStatic(TaleUtils.class);
            mockedTaleUtils.when(() -> TaleUtils.cleanXSS(anyString()))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Mock EmojiParser.parseToAliases to pass through
            mockedEmoji = mockStatic(EmojiParser.class);
            mockedEmoji.when(() -> EmojiParser.parseToAliases(anyString()))
                    .thenAnswer(inv -> inv.getArgument(0));
        } catch (Exception e) {
            try { if (mockedEmoji != null) mockedEmoji.close(); } catch (Exception ex) { /* ignore */ }
            try { if (mockedTaleUtils != null) mockedTaleUtils.close(); } catch (Exception ex) { /* ignore */ }
            try { mockedStatic.close(); } catch (Exception ex) { /* ignore */ }
            throw e;
        }
    }

    @AfterEach
    void tearDown() {
        try {
            if (mockedCommentsConstruction != null) {
                mockedCommentsConstruction.close();
            }
        } catch (Exception e) { /* ignore */ }
        try { mockedEmoji.close(); } catch (Exception e) { /* ignore */ }
        try { mockedTaleUtils.close(); } catch (Exception e) { /* ignore */ }
        try { mockedStatic.close(); } catch (Exception e) { /* ignore */ }
    }

    // ==================== saveComment tests ====================

    @Test
    void testSaveComment_CleansXSSFromAuthor() {
        // Verify that TaleUtils.cleanXSS is called on the author field
        Comments comment = new Comments();
        comment.setCid(1);
        comment.setAuthor("<script>alert('xss')</script>");
        comment.setContent("Normal content");

        // Article lookup returns null, so ValidatorException is thrown
        // But cleanXSS should have been called before that
        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId(any())).thenReturn(null);

        assertThrows(ValidatorException.class, () -> commentsService.saveComment(comment));
        // Verify cleanXSS was called for both author and content
        mockedTaleUtils.verify(() -> TaleUtils.cleanXSS("<script>alert('xss')</script>"));
        mockedTaleUtils.verify(() -> TaleUtils.cleanXSS("Normal content"));
    }

    @Test
    void testSaveComment_LooksUpArticle() {
        // Verify that saveComment queries the article by cid
        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId(any())).thenReturn(null);

        Comments comment = new Comments();
        comment.setCid(42);
        comment.setAuthor("Author");
        comment.setContent("Content");

        assertThrows(ValidatorException.class, () -> commentsService.saveComment(comment));
        // Verify the article lookup was performed
        verify(mockQuery).byId(42);
    }

    @Test
    void testSaveComment_NonExistingArticle_ThrowsException() {
        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId(any())).thenReturn(null);

        Comments comment = new Comments();
        comment.setCid(999);
        comment.setAuthor("Author");
        comment.setContent("Content");

        assertThrows(ValidatorException.class, () -> commentsService.saveComment(comment));
    }

    // ==================== delete tests ====================

    @Test
    void testDelete_ExistingComment() {
        Contents testArticle = new Contents();
        testArticle.setCid(1);
        testArticle.setCommentsNum(5);

        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId(any())).thenReturn(testArticle);

        Delete mockDelete = mock(Delete.class);
        AnimaQuery mockDeleteQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::delete).thenReturn(mockDelete);
        when(mockDelete.from(any(Class.class))).thenReturn(mockDeleteQuery);

        Update mockUpdate = mock(Update.class);
        AnimaQuery mockUpdateQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::update).thenReturn(mockUpdate);
        when(mockUpdate.from(any(Class.class))).thenReturn(mockUpdateQuery);

        commentsService.delete(10, 1);

        // Verify Anima.delete().from(Comments.class).deleteById(10) was called
        verify(mockDeleteQuery).deleteById(10);
        // Verify article comment count was decremented
        verify(mockUpdateQuery).updateById(1);
    }

    @Test
    void testDelete_CommentCountAlreadyZero_NoDecrement() {
        Contents testArticle = new Contents();
        testArticle.setCid(1);
        testArticle.setCommentsNum(0);

        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId(any())).thenReturn(testArticle);

        Delete mockDelete = mock(Delete.class);
        AnimaQuery mockDeleteQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::delete).thenReturn(mockDelete);
        when(mockDelete.from(any(Class.class))).thenReturn(mockDeleteQuery);

        commentsService.delete(10, 1);

        verify(mockDeleteQuery).deleteById(10);
        mockedStatic.verify(Anima::update, never());
    }

    @Test
    void testDelete_ArticleNotFound_StillDeletesComment() {
        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId(any())).thenReturn(null);

        Delete mockDelete = mock(Delete.class);
        AnimaQuery mockDeleteQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::delete).thenReturn(mockDelete);
        when(mockDelete.from(any(Class.class))).thenReturn(mockDeleteQuery);

        commentsService.delete(10, 999);

        verify(mockDeleteQuery).deleteById(10);
        mockedStatic.verify(Anima::update, never());
    }

    // ==================== getComments tests ====================

    @Test
    void testGetComments_ValidArticleId() {
        @SuppressWarnings("unchecked")
        Page<Comments> mockCommentsPage = mock(Page.class);
        @SuppressWarnings("unchecked")
        Page<Comment> mockMappedPage = mock(Page.class);

        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Comments> mockQuery = (AnimaQuery<Comments>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        doReturn(mockCommentsPage).when(mockQuery).page(1, 10);
        doReturn(mockMappedPage).when(mockCommentsPage).map(any());

        Page<Comment> result = commentsService.getComments(1, 1, 10);

        assertNotNull(result);
        assertEquals(mockMappedPage, result);
    }

    @Test
    void testGetComments_NullArticleId_ReturnsNull() {
        Page<Comment> result = commentsService.getComments(null, 1, 10);
        assertNull(result);
    }

    // ==================== getCommentCount tests ====================

    @Test
    void testGetCommentCount_ValidArticleId() {
        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Comments> mockQuery = (AnimaQuery<Comments>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.count()).thenReturn(5L);

        long count = commentsService.getCommentCount(1);

        assertEquals(5L, count);
    }

    @Test
    void testGetCommentCount_NullArticleId_ReturnsZero() {
        long count = commentsService.getCommentCount(null);
        assertEquals(0L, count);
    }

    @Test
    void testGetCommentCount_NoComments() {
        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Comments> mockQuery = (AnimaQuery<Comments>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.count()).thenReturn(0L);

        long count = commentsService.getCommentCount(1);
        assertEquals(0L, count);
    }

    // ==================== findComments tests ====================

    @Test
    void testFindComments_DefaultPagination() {
        @SuppressWarnings("unchecked")
        Page<Comments> mockPage = mock(Page.class);

        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Comments> mockQuery = (AnimaQuery<Comments>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        doReturn(mockPage).when(mockQuery).page(1, 12);

        CommentParam param = new CommentParam();
        Page<Comments> result = commentsService.findComments(param);

        assertNotNull(result);
        assertEquals(mockPage, result);
    }

    @Test
    void testFindComments_CustomPagination() {
        @SuppressWarnings("unchecked")
        Page<Comments> mockPage = mock(Page.class);

        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Comments> mockQuery = (AnimaQuery<Comments>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        doReturn(mockPage).when(mockQuery).page(2, 20);

        CommentParam param = new CommentParam();
        param.setPage(2);
        param.setLimit(20);

        Page<Comments> result = commentsService.findComments(param);

        assertNotNull(result);
        assertEquals(mockPage, result);
    }
}
