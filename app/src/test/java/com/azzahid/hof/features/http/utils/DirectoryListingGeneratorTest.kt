package com.azzahid.hof.features.http.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DirectoryListingGeneratorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `generate produces valid HTML`() {
        val dir = tempFolder.newFolder("testdir")
        val html = DirectoryListingGenerator.generate(dir, "/files", "")
        assertTrue(html.startsWith("<!DOCTYPE html>"))
        assertTrue(html.contains("<html>"))
        assertTrue(html.contains("</html>"))
    }

    @Test
    fun `generate shows directory title`() {
        val dir = tempFolder.newFolder("testdir")
        val html = DirectoryListingGenerator.generate(dir, "/files", "")
        assertTrue(html.contains("Directory listing for /files"))
    }

    @Test
    fun `generate shows title with relative path`() {
        val dir = tempFolder.newFolder("testdir")
        val html = DirectoryListingGenerator.generate(dir, "/files", "subdir")
        assertTrue(html.contains("Directory listing for /files/subdir"))
    }

    @Test
    fun `generate lists files with links`() {
        val dir = tempFolder.newFolder("testdir")
        java.io.File(dir, "readme.txt").writeText("hello")

        val html = DirectoryListingGenerator.generate(dir, "/files", "")
        assertTrue(html.contains("readme.txt"))
        assertTrue(html.contains("href=\"/files/readme.txt\""))
    }

    @Test
    fun `generate lists directories with trailing slash`() {
        val dir = tempFolder.newFolder("testdir")
        java.io.File(dir, "subdir").mkdir()

        val html = DirectoryListingGenerator.generate(dir, "/files", "")
        assertTrue(html.contains("subdir/"))
        assertTrue(html.contains("class=\"dir\""))
    }

    @Test
    fun `generate shows file sizes`() {
        val dir = tempFolder.newFolder("testdir")
        val file = java.io.File(dir, "data.bin")
        file.writeBytes(ByteArray(512))

        val html = DirectoryListingGenerator.generate(dir, "/files", "")
        assertTrue(html.contains("512 bytes"))
    }

    @Test
    fun `generate does not show size for directories`() {
        val dir = tempFolder.newFolder("testdir")
        java.io.File(dir, "subdir").mkdir()

        val html = DirectoryListingGenerator.generate(dir, "/files", "")
        assertFalse(html.contains("bytes") && html.contains("subdir"))
    }

    @Test
    fun `generate shows parent directory link for subpaths`() {
        val dir = tempFolder.newFolder("testdir")
        val html = DirectoryListingGenerator.generate(dir, "/files", "sub/deep")
        assertTrue(html.contains("[Parent Directory]"))
        assertTrue(html.contains("href=\"/files/sub/\""))
    }

    @Test
    fun `generate shows parent directory link to base for first level`() {
        val dir = tempFolder.newFolder("testdir")
        val html = DirectoryListingGenerator.generate(dir, "/files", "subdir")
        assertTrue(html.contains("[Parent Directory]"))
        assertTrue(html.contains("href=\"/files/\""))
    }

    @Test
    fun `generate does not show parent directory link at root`() {
        val dir = tempFolder.newFolder("testdir")
        val html = DirectoryListingGenerator.generate(dir, "/files", "")
        assertFalse(html.contains("[Parent Directory]"))
    }

    @Test
    fun `generate sorts directories before files`() {
        val dir = tempFolder.newFolder("testdir")
        java.io.File(dir, "zebra.txt").writeText("z")
        java.io.File(dir, "alpha").mkdir()
        java.io.File(dir, "beta.txt").writeText("b")

        val html = DirectoryListingGenerator.generate(dir, "/files", "")
        val dirIndex = html.indexOf("alpha/")
        val fileIndex = html.indexOf("beta.txt")
        assertTrue(dirIndex < fileIndex)
    }

    @Test
    fun `generate handles empty directory`() {
        val dir = tempFolder.newFolder("emptydir")
        val html = DirectoryListingGenerator.generate(dir, "/empty", "")
        assertTrue(html.contains("Directory listing for /empty"))
        assertTrue(html.contains("<hr>"))
    }

    @Test
    fun `generate builds correct URLs with relative path`() {
        val dir = tempFolder.newFolder("testdir")
        java.io.File(dir, "test.txt").writeText("x")

        val html = DirectoryListingGenerator.generate(dir, "/files", "sub")
        assertTrue(html.contains("href=\"/files/sub/test.txt\""))
    }
}
