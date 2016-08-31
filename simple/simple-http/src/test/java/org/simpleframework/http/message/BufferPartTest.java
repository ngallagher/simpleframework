package org.simpleframework.http.message;

import junit.framework.TestCase;
import org.simpleframework.common.buffer.ArrayAllocator;

public class BufferPartTest extends TestCase {

    public void testNoDispositionContextNPE() throws Exception {
        BufferPart bufferPart = new BufferPart(new MockSegment(), new ArrayAllocator().allocate());
        assertNull(bufferPart.getFileName());
        assertNull(bufferPart.getName());
        assertFalse(bufferPart.isFile());
    }
}
