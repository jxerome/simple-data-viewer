package com.mainaud.data.viewer.schema;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataTypeTest {

    @Test
    public void ofShouldReturnValueWhenInt() {
        assertThat(DataType.of("int")).isEqualTo(DataType.VALUE);
    }

    @Test
    public void ofShouldReturnVariableWhenVarchar() {
        assertThat(DataType.of("varchar(20)")).isEqualTo(DataType.VARIABLE);
    }

    @Test
    public void ofShouldReturnOthenWhenBool() {
        assertThat(DataType.of("bool")).isEqualTo(DataType.OTHER);
    }

}
