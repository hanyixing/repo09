package com.tale.test.service;

import com.blade.exception.ValidatorException;
import com.tale.model.entity.Comments;
import com.tale.model.entity.Contents;
import com.tale.service.CommentsService;
import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.core.AnimaQuery;
import io.github.biezhi.anima.core.dml.Delete;
import io.github.biezhi.anima.core.dml.Select;
import io.github.biezhi.anima.core.dml.Update;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CommentsService} (评论保存/审核/删除).
 *
 * <p>JUnit 5 + Mockito. The anima ORM static API is mocked with
 * {@code mockStatic} so no real database is required.</p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CommentsServiceTest {

    private final CommentsService commentsService = new CommentsService();

    // ---------- guard clauses (no DB access) ----------

    @Test
    public void testGetComments() {
        // a null article id short-circuits before any query
        assertNull(commentsService.getComments(null, 1, 10));
    }

    @Test
    public void testGetCommentCountNullCidReturnsZero() {
        assertEquals(0L, commentsService.getCommentCount(null));
    }

    // ---------- happy / branch paths (anima mocked) ----------

    @Test
    public void testGetCommentCount() {
        try (MockedStatic<Anima> anima = mockStatic(Anima.class)) {
            Select select = mock(Select.class);
            AnimaQuery query = mock(AnimaQuery.class, Answers.RETURNS_SELF);

            anima.when(Anima::select).thenReturn(select);
            doReturn(query).when(select).from(any());
            doReturn(7L).when(query).count();

            assertEquals(7L, commentsService.getCommentCount(5));
        }
    }

    @Test
    public void testSaveComment() {
        try (MockedStatic<Anima> anima = mockStatic(Anima.class)) {
            Select select = mock(Select.class);
            AnimaQuery query = mock(AnimaQuery.class, Answers.RETURNS_SELF);

            anima.when(Anima::select).thenReturn(select);
            doReturn(query).when(select).from(any());
            // byId(...) returns null (default) -> the article does not exist

            Comments comment = new Comments();
            comment.setCid(99);
            comment.setAuthor("bob");
            comment.setContent("hello");

            ValidatorException ex = assertThrows(ValidatorException.class,
                    () -> commentsService.saveComment(comment));
            assertEquals("不存在的文章", ex.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try (MockedStatic<Anima> anima = mockStatic(Anima.class)) {
            Delete delete = mock(Delete.class);
            AnimaQuery deleteQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
            Select select = mock(Select.class);
            AnimaQuery query = mock(AnimaQuery.class, Answers.RETURNS_SELF);
            Update update = mock(Update.class);
            AnimaQuery updateQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);

            Contents contents = new Contents();
            contents.setCid(3);
            contents.setCommentsNum(2); // > 0 -> comment count gets decremented

            anima.when(Anima::delete).thenReturn(delete);
            doReturn(deleteQuery).when(delete).from(any());
            anima.when(Anima::select).thenReturn(select);
            doReturn(query).when(select).from(any());
            doReturn(contents).when(query).byId(any());
            anima.when(Anima::update).thenReturn(update);
            doReturn(updateQuery).when(update).from(any());

            commentsService.delete(1, 3);

            // comment count decrement path must be taken since commentsNum > 0
            anima.verify(Anima::update);
        }
    }
}
