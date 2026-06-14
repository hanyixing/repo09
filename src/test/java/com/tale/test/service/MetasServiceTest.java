package com.tale.test.service;

import com.blade.exception.ValidatorException;
import com.tale.model.dto.Types;
import com.tale.model.entity.Contents;
import com.tale.model.entity.Metas;
import com.tale.service.MetasService;
import io.github.biezhi.anima.Anima;
import io.github.biezhi.anima.core.AnimaQuery;
import io.github.biezhi.anima.core.ResultList;
import io.github.biezhi.anima.core.dml.Select;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MetasService} (分类、标签).
 *
 * <p>These are pure JUnit 5 + Mockito unit tests: the anima ORM static entry
 * point ({@code Anima.select()}) is mocked with {@code mockStatic} so no real
 * SQLite database is required.</p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class MetasServiceTest {

    private final MetasService metasService = new MetasService();

    // ---------- guard clauses (no DB access) ----------

    @Test
    public void testGetMetasBlankTypeReturnsNull() {
        assertNull(metasService.getMetas(null));
        assertNull(metasService.getMetas(""));
        assertNull(metasService.getMetas("   "));
    }

    @Test
    public void testGetMetaBlankArgsReturnsNull() {
        assertNull(metasService.getMeta(null, "java"));
        assertNull(metasService.getMeta("tag", null));
        assertNull(metasService.getMeta("", ""));
    }

    @Test
    public void testGetMetaMappingBlankTypeReturnsEmptyMap() {
        Map<String, List<Contents>> mapping = metasService.getMetaMapping("");
        assertNotNull(mapping);
        assertTrue(mapping.isEmpty());
    }

    @Test
    public void testSaveMetasNullCidThrows() {
        ValidatorException ex = assertThrows(ValidatorException.class,
                () -> metasService.saveMetas(null, "java", Types.TAG));
        assertEquals("项目关联id不能为空", ex.getMessage());
    }

    @Test
    public void testSaveMetaEmptyArgsReturnsEarly() {
        // type/name empty -> method returns before touching the database
        assertDoesNotThrow(() -> metasService.saveMeta("", "java", null));
        assertDoesNotThrow(() -> metasService.saveMeta("tag", "", null));
    }

    // ---------- happy paths (anima mocked) ----------

    @Test
    public void testGetMetas() {
        try (MockedStatic<Anima> anima = mockStatic(Anima.class)) {
            Select select = mock(Select.class);
            AnimaQuery query = mock(AnimaQuery.class, Answers.RETURNS_SELF);

            Metas meta = new Metas();
            meta.setMid(1);
            meta.setName("java");
            meta.setType(Types.TAG);

            anima.when(Anima::select).thenReturn(select);
            doReturn(query).when(select).from(any());
            doReturn(Arrays.asList(meta)).when(query).all();

            List<Metas> result = metasService.getMetas(Types.TAG);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("java", result.get(0).getName());
        }
    }

    @Test
    public void testGetMeta() {
        try (MockedStatic<Anima> anima = mockStatic(Anima.class)) {
            Select select = mock(Select.class);
            ResultList resultList = mock(ResultList.class);

            Metas meta = new Metas();
            meta.setMid(2);
            meta.setName("blade");
            meta.setType(Types.CATEGORY);

            anima.when(Anima::select).thenReturn(select);
            doReturn(resultList).when(select).bySQL(any(Class.class), any(String.class), any(), any());
            doReturn(meta).when(resultList).one();

            Metas result = metasService.getMeta(Types.CATEGORY, "blade");

            assertNotNull(result);
            assertEquals("blade", result.getName());
        }
    }

    @Test
    public void testGetMetaMapping() {
        try (MockedStatic<Anima> anima = mockStatic(Anima.class)) {
            Select select = mock(Select.class);
            AnimaQuery query = mock(AnimaQuery.class, Answers.RETURNS_SELF);

            Metas meta = new Metas();
            meta.setMid(1);
            meta.setName("java");
            meta.setType(Types.TAG);

            anima.when(Anima::select).thenReturn(select);
            doReturn(query).when(select).from(any());
            // 1st all(): list of metas, 2nd all(): relationships (empty -> no contents)
            doReturn(Arrays.asList(meta))
                    .doReturn(Collections.emptyList())
                    .when(query).all();

            Map<String, List<Contents>> mapping = metasService.getMetaMapping(Types.TAG);

            assertNotNull(mapping);
            assertEquals(1, mapping.size());
            assertTrue(mapping.containsKey("java"));
            assertTrue(mapping.get("java").isEmpty());
        }
    }
}
