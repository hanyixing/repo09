package com.tale.test.service;

import com.blade.exception.ValidatorException;
import com.tale.model.dto.Types;
import com.tale.model.entity.Contents;
import com.tale.service.ContentsService;
import com.tale.service.MetasService;
import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.core.AnimaQuery;
import io.github.biezhi.anima.core.ResultKey;
import io.github.biezhi.anima.core.ResultList;
import io.github.biezhi.anima.core.dml.Delete;
import io.github.biezhi.anima.core.dml.Select;
import io.github.biezhi.anima.page.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ContentsService} (文章发布/编辑/删除).
 *
 * <p>JUnit 5 + Mockito. {@code publish} delegates to {@link MetasService} which
 * is injected as a mock; the anima ORM static API is mocked with
 * {@code mockStatic} so the tests never touch a real database.</p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ContentsServiceTest {

    @Mock
    private MetasService metasService;

    @InjectMocks
    private ContentsService contentsService;

    // ---------- publish ----------

    @Test
    public void testPublishWithoutAuthorThrows() {
        Contents contents = new Contents(); // authorId == null
        ValidatorException ex = assertThrows(ValidatorException.class,
                () -> contentsService.publish(contents));
        assertEquals("请登录后发布文章", ex.getMessage());
    }

    @Test
    public void testPublish() {
        Contents contents = spy(new Contents());
        contents.setAuthorId(1);
        contents.setContent("hello world");
        contents.setTags("java,blade");
        contents.setCategories("tech");

        ResultKey resultKey = mock(ResultKey.class);
        when(resultKey.asInt()).thenReturn(99);
        doReturn(resultKey).when(contents).save();

        Integer cid = contentsService.publish(contents);

        assertEquals(99, cid.intValue());
        // tags & categories must be persisted as metas linked to the new article
        verify(metasService).saveMetas(99, "java,blade", Types.TAG);
        verify(metasService).saveMetas(99, "tech", Types.CATEGORY);
    }

    // ---------- getContents ----------

    @Test
    public void testGetContents() {
        try (MockedStatic<Anima> anima = mockStatic(Anima.class)) {
            Select select = mock(Select.class);
            AnimaQuery query = mock(AnimaQuery.class, Answers.RETURNS_SELF);

            Contents stored = new Contents();
            stored.setCid(10);
            stored.setContent("body"); // slug is null -> url falls back to /article/{cid}

            anima.when(Anima::select).thenReturn(select);
            doReturn(query).when(select).from(any());
            doReturn(stored).when(query).byId(any());

            Contents result = contentsService.getContents("10");

            assertNotNull(result);
            assertEquals("/article/10", result.getUrl());
        }
    }

    @Test
    public void testGetContentsBySlug() {
        try (MockedStatic<Anima> anima = mockStatic(Anima.class)) {
            Select select = mock(Select.class);
            AnimaQuery query = mock(AnimaQuery.class, Answers.RETURNS_SELF);

            Contents stored = new Contents();
            stored.setCid(2);
            stored.setSlug("my-slug");
            stored.setContent("body");

            anima.when(Anima::select).thenReturn(select);
            doReturn(query).when(select).from(any());
            doReturn(stored).when(query).one();

            Contents result = contentsService.getContents("my-slug");

            assertNotNull(result);
            assertEquals("/my-slug", result.getUrl());
        }
    }

    // ---------- updateArticle ----------

    @Test
    public void testUpdateArticle() {
        try (MockedStatic<Anima> anima = mockStatic(Anima.class)) {
            Delete delete = mock(Delete.class);
            AnimaQuery deleteQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
            anima.when(Anima::delete).thenReturn(delete);
            doReturn(deleteQuery).when(delete).from(any());

            Contents contents = spy(new Contents());
            contents.setCid(7);
            contents.setContent("updated");
            contents.setTags("t1");
            contents.setCategories("c1");
            contents.setType(Types.ARTICLE); // not PAGE -> relationships are cleared
            doReturn(7).when(contents).updateById(any());

            contentsService.updateArticle(contents);

            verify(contents).updateById(7);
            verify(metasService).saveMetas(7, "t1", Types.TAG);
            verify(metasService).saveMetas(7, "c1", Types.CATEGORY);
        }
    }

    // ---------- delete ----------

    @Test
    public void testDelete() {
        try (MockedStatic<Anima> anima = mockStatic(Anima.class)) {
            Select select = mock(Select.class);
            AnimaQuery query = mock(AnimaQuery.class, Answers.RETURNS_SELF);
            Delete delete = mock(Delete.class);
            AnimaQuery deleteQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);

            Contents stored = new Contents();
            stored.setCid(5);
            stored.setContent("x");

            anima.when(Anima::select).thenReturn(select);
            doReturn(query).when(select).from(any());
            doReturn(stored).when(query).byId(any());
            anima.when(Anima::delete).thenReturn(delete);
            doReturn(deleteQuery).when(delete).from(any());

            contentsService.delete(5);

            // article row deleted by primary key
            anima.verify(() -> Anima.deleteById(Contents.class, 5));
        }
    }

    // ---------- getArticles ----------

    @Test
    public void testGetArticles() {
        try (MockedStatic<Anima> anima = mockStatic(Anima.class)) {
            Select select = mock(Select.class);
            ResultList resultList = mock(ResultList.class);
            Page page = mock(Page.class);

            anima.when(Anima::select).thenReturn(select);
            doReturn(resultList).when(select).bySQL(any(Class.class), any(String.class), any());
            doReturn(page).when(resultList).page(anyInt(), anyInt());

            Page<Contents> result = contentsService.getArticles(1, 1, 10);

            assertSame(page, result);
        }
    }
}
