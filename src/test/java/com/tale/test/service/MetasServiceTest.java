package com.tale.test.service;

import com.blade.exception.ValidatorException;
import com.tale.model.dto.Types;
import com.tale.model.entity.Contents;
import com.tale.model.entity.Metas;
import com.tale.model.entity.Relationships;
import com.tale.service.MetasService;
import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.core.AnimaQuery;
import io.github.biezhi.anima.core.ResultKey;
import io.github.biezhi.anima.core.ResultList;
import io.github.biezhi.anima.core.dml.Delete;
import io.github.biezhi.anima.core.dml.Select;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * MetasService unit tests
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MetasServiceTest {

    @InjectMocks
    private MetasService metasService;

    private MockedStatic<Anima> mockedStatic;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        mockedStatic = mockStatic(Anima.class);
        try {
            Select mockSelect = mock(Select.class);
            AnimaQuery mockQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
            mockedStatic.when(Anima::select).thenReturn(mockSelect);
            lenient().when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);

            Delete mockDelete = mock(Delete.class);
            AnimaQuery mockDeleteQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
            mockedStatic.when(Anima::delete).thenReturn(mockDelete);
            lenient().when(mockDelete.from(any(Class.class))).thenReturn(mockDeleteQuery);
        } catch (Exception e) {
            try { mockedStatic.close(); } catch (Exception ex) { /* ignore */ }
            throw e;
        }
    }

    @AfterEach
    void tearDown() {
        try { mockedStatic.close(); } catch (Exception e) { /* ignore */ }
    }

    // ==================== getMetas tests ====================

    @Test
    void testGetMetas_Category() {
        Metas meta1 = new Metas();
        meta1.setMid(1);
        meta1.setName("category1");
        meta1.setType(Types.CATEGORY);
        Metas meta2 = new Metas();
        meta2.setMid(2);
        meta2.setName("category2");
        meta2.setType(Types.CATEGORY);

        Select mockSelect = mock(Select.class);
        AnimaQuery<Metas> mockQuery = (AnimaQuery<Metas>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.all()).thenReturn(Arrays.asList(meta1, meta2));

        List<Metas> result = metasService.getMetas(Types.CATEGORY);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetMetas_Tag() {
        Metas tag = new Metas();
        tag.setMid(3);
        tag.setName("java");
        tag.setType(Types.TAG);

        Select mockSelect = mock(Select.class);
        AnimaQuery<Metas> mockQuery = (AnimaQuery<Metas>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.all()).thenReturn(Collections.singletonList(tag));

        List<Metas> result = metasService.getMetas(Types.TAG);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("java", result.get(0).getName());
    }

    @Test
    void testGetMetas_NullType_ReturnsNull() {
        assertNull(metasService.getMetas(null));
    }

    @Test
    void testGetMetas_BlankType_ReturnsNull() {
        assertNull(metasService.getMetas(""));
    }

    @Test
    void testGetMetas_EmptyResult() {
        Select mockSelect = mock(Select.class);
        AnimaQuery<Metas> mockQuery = (AnimaQuery<Metas>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.all()).thenReturn(Collections.emptyList());

        List<Metas> result = metasService.getMetas(Types.CATEGORY);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getMetaMapping tests ====================

    @Test
    void testGetMetaMapping_WithArticles() {
        Metas meta = new Metas();
        meta.setMid(1);
        meta.setName("tech");
        meta.setType(Types.CATEGORY);

        Select mockSelect = mock(Select.class);
        AnimaQuery mockQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.all())
                .thenReturn(Collections.singletonList(meta))
                .thenReturn(Collections.emptyList());

        Map<String, List<Contents>> result = metasService.getMetaMapping(Types.CATEGORY);
        assertNotNull(result);
        assertTrue(result.containsKey("tech"));
    }

    @Test
    void testGetMetaMapping_NullType() {
        Map<String, List<Contents>> result = metasService.getMetaMapping(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetMetaMapping_EmptyMetas() {
        Select mockSelect = mock(Select.class);
        AnimaQuery<Metas> mockQuery = (AnimaQuery<Metas>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.all()).thenReturn(Collections.emptyList());

        Map<String, List<Contents>> result = metasService.getMetaMapping(Types.TAG);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getMeta tests ====================

    @Test
    void testGetMeta_ValidTypeAndName() {
        Metas meta = new Metas();
        meta.setMid(1);
        meta.setName("java");
        meta.setType(Types.TAG);

        ResultList<Metas> mockResultList = mock(ResultList.class);
        Select mockSelect = mock(Select.class);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.bySQL(any(Class.class), anyString(), any(), any())).thenReturn(mockResultList);
        when(mockResultList.one()).thenReturn(meta);

        Metas result = metasService.getMeta(Types.TAG, "java");
        assertNotNull(result);
        assertEquals("java", result.getName());
    }

    @Test
    void testGetMeta_NotFound() {
        ResultList<Metas> mockResultList = mock(ResultList.class);
        Select mockSelect = mock(Select.class);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.bySQL(any(Class.class), anyString(), any(), any())).thenReturn(mockResultList);
        when(mockResultList.one()).thenReturn(null);

        assertNull(metasService.getMeta(Types.TAG, "nonexistent"));
    }

    @Test
    void testGetMeta_NullType() {
        assertNull(metasService.getMeta(null, "java"));
    }

    @Test
    void testGetMeta_BlankName() {
        assertNull(metasService.getMeta(Types.TAG, ""));
    }

    // ==================== saveMetas tests ====================

    @Test
    void testSaveMetas_NewMeta() {
        Select mockSelect = mock(Select.class);
        AnimaQuery<Metas> mockQuery = (AnimaQuery<Metas>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.one()).thenReturn(null);

        try (MockedConstruction<Metas> mockedMetas = mockConstruction(Metas.class, (mock, ctx) -> {
                Metas real = new Metas();
                ResultKey saveResult = mock(ResultKey.class);
                when(saveResult.asInt()).thenReturn(1);
                doReturn(saveResult).when(mock).save();
                doAnswer(inv -> { real.setName(inv.getArgument(0)); return null; }).when(mock).setName(anyString());
                doAnswer(inv -> real.getName()).when(mock).getName();
                doAnswer(inv -> { real.setType(inv.getArgument(0)); return null; }).when(mock).setType(anyString());
                doAnswer(inv -> real.getType()).when(mock).getType();
                doAnswer(inv -> { real.setSlug(inv.getArgument(0)); return null; }).when(mock).setSlug(anyString());
            })) {

            try (MockedConstruction<Relationships> mockedRels = mockConstruction(Relationships.class, (mock, ctx) -> {
                    Relationships real = new Relationships();
                    AnimaQuery relQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
                    when(mock.where(anyString(), any())).thenReturn(relQuery);
                    when(relQuery.count()).thenReturn(0L);
                    ResultKey saveResult = mock(ResultKey.class);
                    when(mock.save()).thenReturn(saveResult);
                    doAnswer(inv -> { real.setCid(inv.getArgument(0)); return null; }).when(mock).setCid(anyInt());
                    doAnswer(inv -> { real.setMid(inv.getArgument(0)); return null; }).when(mock).setMid(anyInt());
                })) {

                metasService.saveMetas(1, "tag1", Types.TAG);

                boolean saved = false;
                for (Metas m : mockedMetas.constructed()) {
                    try { verify(m).save(); saved = true; break; } catch (Throwable t) {}
                }
                assertTrue(saved, "Metas.save() should be called");
            }
        }
    }

    @Test
    void testSaveMetas_ExistingMeta() {
        Metas existingMeta = new Metas();
        existingMeta.setMid(1);
        existingMeta.setName("tag1");
        existingMeta.setType(Types.TAG);

        Select mockSelect = mock(Select.class);
        AnimaQuery<Metas> mockQuery = (AnimaQuery<Metas>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.one()).thenReturn(existingMeta);

        try (MockedConstruction<Relationships> mockedRels = mockConstruction(Relationships.class, (mock, ctx) -> {
                AnimaQuery relQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
                when(mock.where(anyString(), any())).thenReturn(relQuery);
                when(relQuery.count()).thenReturn(0L);
                ResultKey saveResult = mock(ResultKey.class);
                when(mock.save()).thenReturn(saveResult);
            })) {

            metasService.saveMetas(1, "tag1", Types.TAG);

            boolean saved = false;
            for (Relationships r : mockedRels.constructed()) {
                try { verify(r).save(); saved = true; break; } catch (Throwable t) {}
            }
            assertTrue(saved, "Relationships.save() should be called");
        }
    }

    @Test
    void testSaveMetas_NullCid_ThrowsException() {
        assertThrows(ValidatorException.class, () -> metasService.saveMetas(null, "tag1", Types.TAG));
    }

    @Test
    void testSaveMetas_EmptyNames_NoOperation() {
        metasService.saveMetas(1, "", Types.TAG);
    }

    @Test
    void testSaveMetas_NullNames_NoOperation() {
        metasService.saveMetas(1, null, Types.TAG);
    }

    @Test
    void testSaveMetas_MultipleNames() {
        Select mockSelect = mock(Select.class);
        AnimaQuery<Metas> mockQuery = (AnimaQuery<Metas>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.one()).thenReturn(null);

        try (MockedConstruction<Metas> mockedMetas = mockConstruction(Metas.class, (mock, ctx) -> {
                Metas real = new Metas();
                ResultKey saveResult = mock(ResultKey.class);
                when(saveResult.asInt()).thenReturn(ctx.getCount());
                doReturn(saveResult).when(mock).save();
                doAnswer(inv -> { real.setName(inv.getArgument(0)); return null; }).when(mock).setName(anyString());
                doAnswer(inv -> real.getName()).when(mock).getName();
                doAnswer(inv -> { real.setType(inv.getArgument(0)); return null; }).when(mock).setType(anyString());
                doAnswer(inv -> { real.setSlug(inv.getArgument(0)); return null; }).when(mock).setSlug(anyString());
            })) {

            try (MockedConstruction<Relationships> mockedRels = mockConstruction(Relationships.class, (mock, ctx) -> {
                    AnimaQuery relQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
                    when(mock.where(anyString(), any())).thenReturn(relQuery);
                    when(relQuery.count()).thenReturn(0L);
                    ResultKey saveResult = mock(ResultKey.class);
                    when(mock.save()).thenReturn(saveResult);
                })) {

                metasService.saveMetas(1, "tag1,tag2", Types.TAG);

                int saveCount = 0;
                for (Metas m : mockedMetas.constructed()) {
                    try { verify(m).save(); saveCount++; } catch (Throwable t) {}
                }
                assertEquals(2, saveCount, "Should save 2 Metas");
            }
        }
    }

    // ==================== saveMeta tests ====================

    @Test
    void testSaveMeta_NewMeta() {
        Select mockSelect = mock(Select.class);
        AnimaQuery<Metas> mockQuery = (AnimaQuery<Metas>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.one()).thenReturn(null);

        try (MockedConstruction<Metas> mockedMetas = mockConstruction(Metas.class, (mock, ctx) -> {
                Metas real = new Metas();
                ResultKey saveResult = mock(ResultKey.class);
                doReturn(saveResult).when(mock).save();
                doAnswer(inv -> { real.setName(inv.getArgument(0)); return null; }).when(mock).setName(anyString());
                doAnswer(inv -> real.getName()).when(mock).getName();
                doAnswer(inv -> { real.setType(inv.getArgument(0)); return null; }).when(mock).setType(anyString());
                doAnswer(inv -> real.getType()).when(mock).getType();
            })) {

            metasService.saveMeta(Types.CATEGORY, "newCategory", null);

            boolean saved = false;
            for (Metas m : mockedMetas.constructed()) {
                try {
                    verify(m).save();
                    saved = true;
                    assertEquals("newCategory", m.getName());
                    assertEquals(Types.CATEGORY, m.getType());
                    break;
                } catch (Throwable t) {}
            }
            assertTrue(saved, "saveMeta should save a new Metas");
        }
    }

    @Test
    void testSaveMeta_WithMid_UpdateExisting() {
        Select mockSelect = mock(Select.class);
        AnimaQuery<Metas> mockQuery = (AnimaQuery<Metas>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.one()).thenReturn(null);

        try (MockedConstruction<Metas> mockedMetas = mockConstruction(Metas.class, (mock, ctx) -> {
                Metas real = new Metas();
                ResultKey saveResult = mock(ResultKey.class);
                doReturn(saveResult).when(mock).save();
                doAnswer(inv -> { real.setName(inv.getArgument(0)); return null; }).when(mock).setName(anyString());
                doAnswer(inv -> real.getName()).when(mock).getName();
            })) {

            metasService.saveMeta(Types.CATEGORY, "updatedCategory", 5);

            boolean updated = false;
            for (Metas m : mockedMetas.constructed()) {
                try {
                    verify(m).updateById(5);
                    updated = true;
                    assertEquals("updatedCategory", m.getName());
                    break;
                } catch (Throwable t) {}
            }
            assertTrue(updated, "saveMeta with mid should call updateById");
        }
    }

    @Test
    void testSaveMeta_DuplicateThrows() {
        Metas existingMeta = new Metas();
        existingMeta.setMid(1);
        existingMeta.setName("existing");
        existingMeta.setType(Types.CATEGORY);

        Select mockSelect = mock(Select.class);
        AnimaQuery<Metas> mockQuery = (AnimaQuery<Metas>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.one()).thenReturn(existingMeta);

        assertThrows(ValidatorException.class, () ->
                metasService.saveMeta(Types.CATEGORY, "existing", null));
    }

    @Test
    void testSaveMeta_EmptyType_NoOperation() {
        metasService.saveMeta("", "name", null);
    }

    @Test
    void testSaveMeta_EmptyName_NoOperation() {
        metasService.saveMeta(Types.CATEGORY, "", null);
    }

    // ==================== delete tests ====================

    @Test
    void testDelete_ExistingMeta() {
        Metas testMeta = new Metas();
        testMeta.setMid(1);
        testMeta.setName("category1");
        testMeta.setType(Types.CATEGORY);

        Select mockSelect = mock(Select.class);
        AnimaQuery mockQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId(1)).thenReturn(testMeta);
        when(mockQuery.all()).thenReturn(Collections.emptyList());

        mockedStatic.when(() -> Anima.deleteById(any(Class.class), anyInt())).thenReturn(1);

        try (MockedConstruction<Contents> mc = mockConstruction(Contents.class)) {
            metasService.delete(1);
            mockedStatic.verify(() -> Anima.deleteById(Metas.class, 1));
        }
    }

    @Test
    void testDelete_WithRelationships_UpdatesArticles() {
        Metas testMeta = new Metas();
        testMeta.setMid(1);
        testMeta.setName("oldCategory");
        testMeta.setType(Types.CATEGORY);

        Relationships rel = new Relationships();
        rel.setCid(10);
        rel.setMid(1);

        Contents relatedArticle = new Contents();
        relatedArticle.setCid(10);
        relatedArticle.setCategories("oldCategory,other");
        relatedArticle.setTags("someTag");

        Select mockSelect = mock(Select.class);
        AnimaQuery mockQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId(1)).thenReturn(testMeta);
        when(mockQuery.all()).thenReturn(Collections.singletonList(rel));
        when(mockQuery.byId(10)).thenReturn(relatedArticle);

        mockedStatic.when(() -> Anima.deleteById(any(Class.class), anyInt())).thenReturn(1);

        Delete mockDelete = mock(Delete.class);
        AnimaQuery mockDeleteQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::delete).thenReturn(mockDelete);
        when(mockDelete.from(any(Class.class))).thenReturn(mockDeleteQuery);

        try (MockedConstruction<Contents> mc = mockConstruction(Contents.class)) {
            metasService.delete(1);

            boolean updated = false;
            for (Contents c : mc.constructed()) {
                try { verify(c).updateById(10); updated = true; break; } catch (Throwable t) {}
            }
            assertTrue(updated, "Should update the related article");
        }
    }

    @Test
    void testDelete_TagType_UpdatesArticleTags() {
        Metas testMeta = new Metas();
        testMeta.setMid(2);
        testMeta.setName("oldTag");
        testMeta.setType(Types.TAG);

        Relationships rel = new Relationships();
        rel.setCid(20);
        rel.setMid(2);

        Contents relatedArticle = new Contents();
        relatedArticle.setCid(20);
        relatedArticle.setTags("oldTag,keepTag");
        relatedArticle.setCategories("someCat");

        Select mockSelect = mock(Select.class);
        AnimaQuery mockQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId(2)).thenReturn(testMeta);
        when(mockQuery.all()).thenReturn(Collections.singletonList(rel));
        when(mockQuery.byId(20)).thenReturn(relatedArticle);

        mockedStatic.when(() -> Anima.deleteById(any(Class.class), anyInt())).thenReturn(1);

        Delete mockDelete = mock(Delete.class);
        AnimaQuery mockDeleteQuery = mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::delete).thenReturn(mockDelete);
        when(mockDelete.from(any(Class.class))).thenReturn(mockDeleteQuery);

        try (MockedConstruction<Contents> mc = mockConstruction(Contents.class)) {
            metasService.delete(2);

            boolean updated = false;
            for (Contents c : mc.constructed()) {
                try { verify(c).updateById(20); updated = true; break; } catch (Throwable t) {}
            }
            assertTrue(updated, "Should update the related article for tag deletion");
        }
    }

    @Test
    void testDelete_NonExistingMeta_NoDeleteCalled() {
        Select mockSelect = mock(Select.class);
        AnimaQuery<Metas> mockQuery = (AnimaQuery<Metas>) mock(AnimaQuery.class, Answers.RETURNS_SELF);
        mockedStatic.when(Anima::select).thenReturn(mockSelect);
        when(mockSelect.from(any(Class.class))).thenReturn(mockQuery);
        when(mockQuery.byId(999)).thenReturn(null);

        try (MockedConstruction<Contents> mc = mockConstruction(Contents.class)) {
            metasService.delete(999);
            mockedStatic.verify(() -> Anima.deleteById(any(Class.class), anyInt()), never());
        }
    }
}
