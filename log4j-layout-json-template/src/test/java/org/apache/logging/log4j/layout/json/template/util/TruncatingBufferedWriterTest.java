package org.apache.logging.log4j.layout.json.template.util;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TruncatingBufferedWriterTest {

    @Test
    public void test_ctor_invalid_args() {
        Assertions
                .assertThatThrownBy(() -> new TruncatingBufferedWriter(-1))
                .isInstanceOf(NegativeArraySizeException.class);
    }

    @Test
    public void test_okay_payloads() {

        // Fill in the writer.
        final int capacity = 1_000;
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(capacity);
        writer.write(Character.MAX_VALUE);
        writer.write(new char[]{Character.MIN_VALUE, Character.MAX_VALUE});
        writer.write("foo");
        writer.write("foobar", 3, 3);
        writer.write(new char[]{'f', 'o', 'o', 'b', 'a', 'r', 'b', 'u', 'z', 'z'}, 6, 4);
        writer.append('!');
        writer.append("yo");
        writer.append(null);
        writer.append("yo dog", 3, 6);
        writer.append(null, -1, -1);

        // Verify accessors.
        final char[] expectedBuffer = new char[capacity];
        int expectedPosition = 0;
        expectedBuffer[expectedPosition++] = Character.MAX_VALUE;
        expectedBuffer[expectedPosition++] = Character.MIN_VALUE;
        expectedBuffer[expectedPosition++] = Character.MAX_VALUE;
        expectedBuffer[expectedPosition++] = 'f';
        expectedBuffer[expectedPosition++] = 'o';
        expectedBuffer[expectedPosition++] = 'o';
        expectedBuffer[expectedPosition++] = 'b';
        expectedBuffer[expectedPosition++] = 'a';
        expectedBuffer[expectedPosition++] = 'r';
        expectedBuffer[expectedPosition++] = 'b';
        expectedBuffer[expectedPosition++] = 'u';
        expectedBuffer[expectedPosition++] = 'z';
        expectedBuffer[expectedPosition++] = 'z';
        expectedBuffer[expectedPosition++] = '!';
        expectedBuffer[expectedPosition++] = 'y';
        expectedBuffer[expectedPosition++] = 'o';
        expectedBuffer[expectedPosition++] = 'n';
        expectedBuffer[expectedPosition++] = 'u';
        expectedBuffer[expectedPosition++] = 'l';
        expectedBuffer[expectedPosition++] = 'l';
        expectedBuffer[expectedPosition++] = 'd';
        expectedBuffer[expectedPosition++] = 'o';
        expectedBuffer[expectedPosition++] = 'g';
        expectedBuffer[expectedPosition++] = 'n';
        expectedBuffer[expectedPosition++] = 'u';
        expectedBuffer[expectedPosition++] = 'l';
        expectedBuffer[expectedPosition++] = 'l';
        Assertions.assertThat(writer.getBuffer()).isEqualTo(expectedBuffer);
        Assertions.assertThat(writer.getPosition()).isEqualTo(expectedPosition);
        Assertions.assertThat(writer.getCapacity()).isEqualTo(capacity);
        Assertions.assertThat(writer.isTruncated()).isFalse();
        verifyClose(writer);

    }

    @Test
    public void test_write_int_truncation() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        writer.write('a');
        writer.write('b');
        verifyTruncation(writer, 'a');
    }

    @Test
    public void test_write_char_array_truncation() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        writer.write(new char[]{'a', 'b'});
        verifyTruncation(writer, 'a');
    }

    @Test
    public void test_write_String_truncation() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        writer.write("ab");
        verifyTruncation(writer, 'a');
    }

    @Test
    public void test_write_String_slice_invalid_args() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        final String string = "a";
        Assertions
                .assertThatThrownBy(() -> writer.write(string, -1, 1))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid offset");
        Assertions
                .assertThatThrownBy(() -> writer.write(string, 1, 1))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid offset");
        Assertions
                .assertThatThrownBy(() -> writer.write(string, 0, -1))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid length");
        Assertions
                .assertThatThrownBy(() -> writer.write(string, 0, 2))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid length");
    }

    @Test
    public void test_write_String_slice_truncation() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        writer.write("ab", 0, 2);
        verifyTruncation(writer, 'a');
    }

    @Test
    public void test_write_char_array_slice_invalid_args() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        final char[] buffer = new char[]{'a'};
        Assertions
                .assertThatThrownBy(() -> writer.write(buffer, -1, 1))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid offset");
        Assertions
                .assertThatThrownBy(() -> writer.write(buffer, 1, 1))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid offset");
        Assertions
                .assertThatThrownBy(() -> writer.write(buffer, 0, -1))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid length");
        Assertions
                .assertThatThrownBy(() -> writer.write(buffer, 0, 2))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid length");
    }

    @Test
    public void test_write_char_array_slice_truncation() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        writer.write(new char[]{'a', 'b'}, 0, 2);
        verifyTruncation(writer, 'a');
    }

    @Test
    public void test_append_char_truncation() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        writer.append('a');
        writer.append('b');
        verifyTruncation(writer, 'a');
    }

    @Test
    public void test_append_seq_truncation() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        writer.append("ab");
        verifyTruncation(writer, 'a');
    }

    @Test
    public void test_append_seq_null_truncation() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        writer.append(null);
        verifyTruncation(writer, 'n');
    }

    @Test
    public void test_append_seq_slice_invalid_args() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        final CharSequence seq = "ab";
        Assertions
                .assertThatThrownBy(() -> writer.append(seq, -1, 2))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid start");
        Assertions
                .assertThatThrownBy(() -> writer.append(seq, 2, 2))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid start");
        Assertions
                .assertThatThrownBy(() -> writer.append(seq, 0, -1))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid end");
        Assertions
                .assertThatThrownBy(() -> writer.append(seq, 1, 0))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid end");
        Assertions
                .assertThatThrownBy(() -> writer.append(seq, 0, 3))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageStartingWith("invalid end");
    }

    @Test
    public void test_append_seq_slice_truncation() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        writer.append("ab", 0, 1);
        verifyTruncation(writer, 'a');
    }

    @Test
    public void test_append_seq_slice_null_truncation() {
        final TruncatingBufferedWriter writer = new TruncatingBufferedWriter(1);
        writer.append(null, -1, -1);
        verifyTruncation(writer, 'n');
    }

    private void verifyTruncation(
            final TruncatingBufferedWriter writer,
            final char c) {
        Assertions.assertThat(writer.getBuffer()).isEqualTo(new char[]{c});
        Assertions.assertThat(writer.getPosition()).isEqualTo(1);
        Assertions.assertThat(writer.getCapacity()).isEqualTo(1);
        Assertions.assertThat(writer.isTruncated()).isTrue();
        verifyClose(writer);
    }

    private void verifyClose(final TruncatingBufferedWriter writer) {
        writer.close();
        Assertions.assertThat(writer.getPosition()).isEqualTo(0);
        Assertions.assertThat(writer.isTruncated()).isFalse();
    }

}
