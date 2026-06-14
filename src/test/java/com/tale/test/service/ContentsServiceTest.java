package com.tale.test.service;

import com.blade.exception.ValidatorException;
import com.tale.model.dto.Types;
import com.tale.model.entity.Comments;
import com.tale.model.entity.Contents;
import com.tale.model.entity.Relationships;
import com.tale.model.params.ArticleParam;
import com.tale.service.ContentsService;
import com.tale.service.MetasService;
import com.vdurmont.emoji.EmojiParser;
import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.core.AnimaQuery;
import io.github.biezhi.anima.core.ResultKey;
import io.github.biezhi.anima.core.ResultList;
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
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ContentsService unit tests
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContentsServiceTest {

    @InjectMocks
    private ContentsService contentsService;

    @Mock
    private MetasService metasService;

    private MockedStatic<Anima> mockedStatic;
    private MockedStatic<EmojiParser> mockedEmoji;
    private MockedConstruction<Contents> mockedContentsConstruction;
    private MockedConstruction<Relationships> mockedRelationshipsConstruction;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        mockedStatic = mockStatic(Anima.class);
        try {
            // Anima.select() -> Select -> AnimaQuery chain
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

            // Mock EmojiParser.parseToAliases to pass through
            mockedEmoji = mockStatic(EmojiParser.class);
            mockedEmoji.when(() -> EmojiParser.parseToAliases(anyString()))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Mock new Contents() construction - use spy so real getters/setters work
            mockedContentsConstruction = mockConstruction(Contents.class, (mock, context) -> {
                ResultKey resultKey = mock(ResultKey.class);
                lenient().when(resultKey.asInt()).thenReturn(99);
                lenient().doReturn(resultKey).when(mock).save();
                // Use spy delegation for real method behavior
                Contents real = new Contents();
                lenient().doAnswer(inv -> { real.setCid(inv.getArgument(0)); return null; }).when(mock).setCid(anyInt());
                lenient().doAnswer(inv -> real.getCid()).when(mock).getCid();
                lenient().doAnswer(inv -> { real.setAuthorId(inv.getArgument(0)); return null; }).when(mock).setAuthorId(any());
                lenient().doAnswer(inv -> real.getAuthorId()).when(mock).getAuthorId();
                lenient().doAnswer(inv -> { real.setTitle(inv.getArgument(0)); return null; }).when(mock).setTitle(anyString());
                lenient().doAnswer(inv -> real.getTitle()).when(mock).getTitle();
                lenient().doAnswer(inv -> { real.setContent(inv.getArgument(0)); return null; }).when(mock).setContent(anyString());
                lenient().doAnswer(inv -> real.getContent()).when(mock).getContent();
                lenient().doAnswer(inv -> { real.setTags(inv.getArgument(0)); return null; }).when(mock).setTags(anyString());
                lenient().doAnswer(inv -> real.getTags()).when(mock).getTags();
                lenient().doAnswer(inv -> { real.setCategories(inv.getArgument(0)); return null; }).when(mock).setCategories(anyString());
                lenient().doAnswer(inv -> real.getCategories()).when(mock).getCategories();
                lenient().doAnswer(inv -> { real.setType(inv.getArgument(0)); return null; }).when(mock).setType(anyString());
                lenient().doAnswer(inv -> real.getType()).when(mock).getType();
                lenient().doAnswer(inv -> { real.setStatus(inv.getArgument(0)); return null; }).when(mock).setStatus(anyString());
                lenient().doAnswer(inv -> real.getStatus()).when(mock).getStatus();
                lenient().doAnswer(inv -> { real.setSlug(inv.getArgument(0)); return null; }).when(mock).setSlug(anyString());
                lenient().doAnswer(inv -> real.getSlug()).when(mock).getSlug();
                lenient().doAnswer(inv -> { real.setHits(inv.getArgument(0)); return null; }).when(mock).setHits(anyInt());
                lenient().doAnswer(inv -> real.getHits()).when(mock).getHits();
                lenient().doAnswer(inv -> { real.setCreated(inv.getArgument(0)); return null; }).when(mock).setCreated(anyInt());
                lenient().doAnswer(inv -> real.getCreated()).when(mock).getCreated();
                lenient().doAnswer(inv -> { real.setModified(inv.getArgument(0)); return null; }).when(mock).setModified(anyInt());
                lenient().doAnswer(inv -> real.getModified()).when(mock).getModified();
                lenient().doAnswer(inv -> { real.setUrl(inv.getArgument(0)); return null; }).when(mock).setUrl(anyString());
                lenient().doAnswer(inv -> real.getUrl()).when(mock).getUrl();
                lenient().doAnswer(inv -> { real.setCommentsNum(inv.getArgument(0)); return null; }).when(mock).setCommentsNum(anyInt());
                lenient().doAnswer(inv -> real.getCommentsNum()).when(mock).getCommentsNum();
            });

            // Mock new Relationships() construction
            mockedRelationshipsConstruction = mockConstruction(Relationships.class, (mock, context) -> {
                ResultKey resultKey = mock(ResultKey.class);
                lenient().when(mock.save()).thenReturn(resultKey);
            });
        } catch (Exception e) {
            try { if (mockedEmoji != null) mockedEmoji.close(); } catch (Exception ex) { /* ignore */ }
            try { mockedStatic.close(); } catch (Exception ex) { /* ignore */ }
            throw e;
        }
    }

    @AfterEach
    void tearDown() {
        try { mockedRelationshipsConstruction.close(); } catch (Exception e) { /* ignore */ }
        try { mockedContentsConstruction.close(); } catch (Exception e) { /* ignore */ }
        try { mockedEmoji.close(); } catch (Exception e) { /* ignore */ }
        try { mockedStatic.close(); } catch (Exception e) { /* ignore */ }
    }

    // ==================== getContents tests ====================

    @Test
    void testGetContents_ById() {
        Contents testArticle = new Contents();
        testArticle.setCid(1);
        testArticle.setTitle("Test Article");
        testArticle.setSlug("test-article");
        testArticle.setContent("Hello World");

        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        // byId receives String "1" (not int 1) since getContents passes the string arg
        when(mockQuery.byId("1")).thenReturn(testArticle);
        when(mockQuery.byId(1)).thenReturn(testArticle);
        when(mockQuery.one()).thenReturn(testArticle);

        Contents result = contentsService.getContents("1");

        assertNotNull(result);
        assertEquals(1, result.getCid());
        assertEquals("Test Article", result.getTitle());
        assertEquals("/test-article", result.getUrl());
    }

    @Test
    void testGetContents_BySlug() {
        Contents testArticle = new Contents();
        testArticle.setCid(2);
        testArticle.setTitle("First Post");
        testArticle.setSlug("first-post");
        testArticle.setContent("Some content");

        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.one()).thenReturn(testArticle);
        when(mockQuery.byId(any())).thenReturn(testArticle);

        Contents result = contentsService.getContents("first-post");

        assertNotNull(result);
        assertEquals("first-post", result.getSlug());
        assertEquals("/first-post", result.getUrl());
    }

    @Test
    void testGetContents_BlankId() {
        Contents result = contentsService.getContents("");
        assertNull(result);
    }

    @Test
    void testGetContents_NullId() {
        Contents result = contentsService.getContents(null);
        assertNull(result);
    }

    @Test
    void testGetContents_NotFound() {
        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId("999")).thenReturn(null);
        when(mockQuery.byId(999)).thenReturn(null);
        when(mockQuery.one()).thenReturn(null);

        Contents result = contentsService.getContents("999");
        assertNull(result);
    }

    @Test
    void testGetContents_NoSlug_UsesArticlePath() {
        Contents testArticle = new Contents();
        testArticle.setCid(5);
        testArticle.setTitle("No Slug Article");
        testArticle.setContent("content");

        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId("5")).thenReturn(testArticle);
        when(mockQuery.byId(5)).thenReturn(testArticle);
        when(mockQuery.one()).thenReturn(testArticle);

        Contents result = contentsService.getContents("5");

        assertNotNull(result);
        assertEquals("/article/5", result.getUrl());
    }

    @Test
    void testGetContents_ContentUnescape() {
        Contents testArticle = new Contents();
        testArticle.setCid(1);
        testArticle.setTitle("Test");
        testArticle.setSlug("test");
        testArticle.setContent("He said \\\"hello\\\"");

        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId("1")).thenReturn(testArticle);
        when(mockQuery.byId(1)).thenReturn(testArticle);
        when(mockQuery.one()).thenReturn(testArticle);

        Contents result = contentsService.getContents("1");

        assertNotNull(result);
        assertEquals("He said \"hello\"", result.getContent());
    }

    // ==================== publish tests ====================

    @Test
    void testPublish_NewArticle() {
        Contents article = new Contents();
        article.setAuthorId(1);
        article.setTitle("New Article");
        article.setContent("# Hello World");
        article.setTags("java,blade");
        article.setCategories("tech");
        article.setType(Types.ARTICLE);
        article.setStatus(Types.PUBLISH);

        Integer cid = contentsService.publish(article);

        assertEquals(99, cid);
        verify(metasService).saveMetas(99, "java,blade", Types.TAG);
        verify(metasService).saveMetas(99, "tech", Types.CATEGORY);
    }

    @Test
    void testPublish_NoAuthorId_ThrowsException() {
        Contents article = new Contents();
        article.setTitle("No Author");

        assertThrows(ValidatorException.class, () -> contentsService.publish(article));
    }

    @Test
    void testPublish_SetsHitsToZero() {
        Contents article = new Contents();
        article.setAuthorId(1);
        article.setTitle("Test");
        article.setContent("content");
        article.setType(Types.ARTICLE);
        article.setStatus(Types.PUBLISH);

        contentsService.publish(article);

        // With CALLS_REAL_METHODS, the mock's setHits(0) actually sets the field
        assertEquals(0, article.getHits());
    }

    @Test
    void testPublish_SetsTimestamps() {
        Contents article = new Contents();
        article.setAuthorId(1);
        article.setTitle("Test");
        article.setContent("content");
        article.setType(Types.ARTICLE);
        article.setStatus(Types.PUBLISH);

        contentsService.publish(article);

        assertNotNull(article.getCreated());
        assertNotNull(article.getModified());
    }

    // ==================== updateArticle tests ====================

    @Test
    void testUpdateArticle_PostType() {
        Contents article = mock(Contents.class);
        when(article.getCid()).thenReturn(1);
        when(article.getType()).thenReturn(Types.ARTICLE);
        when(article.getTags()).thenReturn("java,python");
        when(article.getCategories()).thenReturn("tech");
        when(article.getContent()).thenReturn("Updated content");
        when(article.getCreated()).thenReturn(1000);

        contentsService.updateArticle(article);

        verify(article).updateById(1);
        verify(metasService).saveMetas(1, "java,python", Types.TAG);
        verify(metasService).saveMetas(1, "tech", Types.CATEGORY);
    }

    @Test
    void testUpdateArticle_PageType_NoDeleteRelationships() {
        Contents article = mock(Contents.class);
        when(article.getCid()).thenReturn(2);
        when(article.getType()).thenReturn(Types.PAGE);
        when(article.getTags()).thenReturn("");
        when(article.getCategories()).thenReturn("");
        when(article.getContent()).thenReturn("Page content");
        when(article.getCreated()).thenReturn(1000);

        contentsService.updateArticle(article);

        verify(article).updateById(2);
        mockedStatic.verify(Anima::delete, never());
    }

    @Test
    void testUpdateArticle_NullTags_DefaultsToEmpty() {
        Contents article = mock(Contents.class);
        when(article.getCid()).thenReturn(1);
        when(article.getType()).thenReturn(Types.ARTICLE);
        when(article.getTags()).thenReturn(null);
        when(article.getCategories()).thenReturn(null);
        when(article.getContent()).thenReturn("content");
        when(article.getCreated()).thenReturn(1000);

        contentsService.updateArticle(article);

        verify(article).setTags("");
        verify(article).setCategories("");
    }

    // ==================== delete tests ====================

    @Test
    void testDelete_ExistingArticle() {
        // delete() first calls getContents(), needs select chain to return article
        Contents testArticle = new Contents();
        testArticle.setCid(1);
        testArticle.setTitle("To Delete");

        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId("1")).thenReturn(testArticle);
        when(mockQuery.byId(1)).thenReturn(testArticle);
        when(mockQuery.one()).thenReturn(testArticle);

        mockedStatic.when(() -> Anima.deleteById(any(Class.class), anyInt())).thenReturn(1);

        contentsService.delete(1);

        mockedStatic.verify(() -> Anima.deleteById(Contents.class, 1));
    }

    @Test
    void testDelete_NonExistingArticle_NoDeleteCalled() {
        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId("999")).thenReturn(null);
        when(mockQuery.byId(999)).thenReturn(null);
        when(mockQuery.one()).thenReturn(null);

        contentsService.delete(999);

        mockedStatic.verify(() -> Anima.deleteById(any(Class.class), anyInt()), never());
    }

    // ==================== getArticles tests ====================

    @Test
    void testGetArticles_ByMid() {
        @SuppressWarnings("unchecked")
        ResultList<Contents> mockResultList = mock(ResultList.class);
        @SuppressWarnings("unchecked")
        Page<Contents> mockPage = mock(Page.class);

        Select mockSelect = mock(Select.class);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.bySQL(eq(Contents.class), anyString(), any())).thenReturn(mockResultList);
        doReturn(mockPage).when(mockResultList).page(1, 10);

        Page<Contents> result = contentsService.getArticles(1, 1, 10);

        assertNotNull(result);
        assertEquals(mockPage, result);
    }

    // ==================== findArticles tests ====================

    @Test
    void testFindArticles_ByStatus() {
        @SuppressWarnings("unchecked")
        Page<Contents> mockPage = mock(Page.class);
        @SuppressWarnings("unchecked")
        Page<Contents> mockMappedPage = mock(Page.class);

        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        doReturn(mockPage).when(mockQuery).page(1, 12);
        doReturn(mockMappedPage).when(mockPage).map(any());

        ArticleParam param = new ArticleParam();
        param.setStatus("publish");
        param.setType("post");
        param.setOrderBy("created desc");

        Page<Contents> result = contentsService.findArticles(param);

        assertNotNull(result);
        assertEquals(mockMappedPage, result);
    }

    @Test
    void testFindArticles_ByTitleAndCategory() {
        @SuppressWarnings("unchecked")
        Page<Contents> mockPage = mock(Page.class);
        @SuppressWarnings("unchecked")
        Page<Contents> mockMappedPage = mock(Page.class);

        Select mockSelect = mock(Select.class);
        @SuppressWarnings("unchecked")
        AnimaQuery<Contents> mockQuery = (AnimaQuery<Contents>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        doReturn(mockPage).when(mockQuery).page(1, 12);
        doReturn(mockMappedPage).when(mockPage).map(any());

        ArticleParam param = new ArticleParam();
        param.setTitle("Hello");
        param.setCategories("tech");
        param.setType("post");
        param.setOrderBy("created desc");

        Page<Contents> result = contentsService.findArticles(param);

        assertNotNull(result);
        assertEquals(mockMappedPage, result);
    }
}
