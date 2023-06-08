package jp.konosuba;

import static org.junit.Assert.assertTrue;

import jp.konosuba.utils.StringUtils;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void printHash() throws NoSuchAlgorithmException {
        System.out.println(StringUtils.getStringFromSHA256(StringUtils.generateCodeStatic(16)));
    }

}
