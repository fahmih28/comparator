package org.obj;

import com.rabbani.sp.core.ComparatorPool;
import com.rabbani.sp.core.Discriminator;
import com.rabbani.sp.core.Result;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class PersonTest {

    @Test
    void given_twoDiffName_theLevelResultWhereNameBelongsShouldReturnIsSameFalseAndTheAttributeWorkAsItShould() {
        Discriminator<Person> discriminator = ComparatorPool.getInstance().get(Person.class);

        Person currentValue = new Person("Fulan", 33, List.of());
        Person newValue = new Person("John", 33, List.of());

        Result result = discriminator.discriminate(currentValue, newValue);

        assertAll("root checking, type " + Result.class.getName(), () -> {
            //isTheSame value will be changed reflected to its subs(), if there is even one subs
            assertFalse(result.isTheSame(), ".isTheSame()");

            assertEquals("$", result.path().canonicalPath(), ".path().canonicalPath()");

            assertNull(result.path().parent(), ".path().parent()");

            assertEquals(Result.Type.ROOT, result.type(), ".type()");

            assertEquals(currentValue, result.value().value(), ".value().value()");

            assertEquals(newValue, result.value().newValue(), ".value().newValue");

            assertNotNull(result.value().newValue(), ".value().newValue()");

            assertNotNull(result.value().value(), ".value().value()");

            assertEquals(3, result.subs().size(), ".sub().size()");
        });

    }
}
