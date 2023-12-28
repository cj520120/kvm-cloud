package cn.chenjun.cloud.test;

import lombok.SneakyThrows;
import org.junit.Before;
import org.libvirt.Connect;
import org.mockito.MockitoAnnotations;

public abstract class AbstractTest {
    protected final Connect connect;

    @SneakyThrows
    protected AbstractTest() {
        connect = new Connect("qemu+tcp://192.168.1.69:16509/system");
    }

    @Before
    @SneakyThrows
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.init();
    }

    protected void init() {

    }
}
