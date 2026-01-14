package yurykorzun.art.universe.music.data.raw.lastfm.test.utils;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertionUtils {

    private AssertionUtils() {}

    private static final CollectionsComparator collectionValuesComparator = new CollectionsValuesComparator();
    private static final CollectionsComparator collectionSizeOnlyComparator = new CollectionsSizeOnlyComparator();

    /**
     * Verifies that a specific method was called expected number of times with expected arguments for each call.
     * Value check is skipped for a call in the sequence if no expected argument is provided.
     * @param invocationVerifier    lambda with verify-call that utilizes ArgumentCaptor.
     * @param clazz                 Class for target method return type.
     * @param expectedArguments     {@link List} of expected argument, in calls order.
     *                              If argument is a subtype of {@link Collection}, then the check will be made only if values are provided
     * @param errMessagePrefix      String containing info about target method, for more readable output.
     * @param <T>                   Target method return type.
     */
    public static <T> void verifyAndAssertInvocations(
        Consumer<ArgumentCaptor<T>> invocationVerifier,
        Class<T> clazz,
        List<T> expectedArguments,
        String errMessagePrefix
    ) {
        verifyAndAssertInvocations(invocationVerifier, clazz, expectedArguments, errMessagePrefix, collectionValuesComparator);
    }

    /**
     * Verifies that target method was invoked a specific number of times.
     */
    public static <T> void verifyInvocationsNumber(
        Consumer<ArgumentCaptor<T>> invocationVerifier,
        Class<T> clazz,
        int expectedInvocationsNumber,
        String errMessagePrefix
    ) {
        // make a list with empty invocation params to disable their check
        List<T> emptyInvocationParams = new ArrayList<>();
        for (int i = 0; i < expectedInvocationsNumber; i++) {
            emptyInvocationParams.add(null);
        }

        verifyAndAssertInvocations(invocationVerifier, clazz, emptyInvocationParams, errMessagePrefix);
    }

    /**
     * Verifies that the method accepting a {@link Collection} as an arg was called expected number of times,
     * with args being collections of provided sizes. Values check is skipped.
     */
    @SuppressWarnings("unchecked")
    public static <T> void verifyInvocationsNumberWithCollectionsSizeOnly(
        Consumer<ArgumentCaptor<List>> invocationVerifier,
        List<Integer> invocationArgumentSizes,
        String errMessagePrefix
    ) {
        // make a list with empty invocation params to disable their check
        List<List> sizedInvocationParams = new ArrayList<>();
        for (int argSize : invocationArgumentSizes) {
            List<Object> invocationParam = new ArrayList<>(argSize);
            for (int j = 0; j < argSize; j++) {
                invocationParam.add(null);
            }
            sizedInvocationParams.add(invocationParam);
        }

        verifyAndAssertInvocations(invocationVerifier, List.class, sizedInvocationParams, errMessagePrefix, collectionSizeOnlyComparator);
    }

    /**
     * Unified verifyAndAssert method that handles both non-collection and collection argument types.
     * For the sake of the latter, a valid {@link CollectionsComparator} must be provided, which is handled by current class's logic.
     */
    private static <T> void verifyAndAssertInvocations(
        Consumer<ArgumentCaptor<T>> invocationVerifier,
        Class<T> clazz,
        List<T> expectedArguments,
        String errMessagePrefix,
        CollectionsComparator comparator
    ) {
        ArgumentCaptor<T> captor = ArgumentCaptor.forClass(clazz);
        invocationVerifier.accept(captor);
        List<T> actualValues = captor.getAllValues();
        assertEquals(expectedArguments.size(), actualValues.size(), String.format("%s Unexpected invocation count", errMessagePrefix));

        for (int i = 0; i < expectedArguments.size(); i++) {

            T expected = expectedArguments.get(i);
            if (expected == null) continue;

            T actual = actualValues.get(i);
            if (expected instanceof Collection && actual instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Object> actualColl = (Collection<Object>) actual;
                @SuppressWarnings("unchecked")
                Collection<Object> expectedColl = (Collection<Object>) expected;
                comparator.compare(expectedColl, actualColl, String.format("%s, iteration %s", errMessagePrefix, i));
            } else {
                // simple comparison
                assertEquals(expected, actual, String.format("%s: Mismatch at invocation ", errMessagePrefix, i));
            }
        }
    }

    private abstract static class CollectionsComparator {

        void compare(Collection<?> expected, Collection<?> actual, String errMessagePrefix) {
            assertEquals(expected.size(), actual.size(),
                String.format("%s: Collection size mismatch", errMessagePrefix));
            compareContents(expected, actual, errMessagePrefix);
        }

        abstract void compareContents(Collection<?> expected, Collection<?> actual, String errMessagePrefix);

    }

    private static class CollectionsValuesComparator extends CollectionsComparator {
        @Override
        void compareContents(Collection<?> expected, Collection<?> actual, String errMessagePrefix) {
            assertThat(
                String.format("%s: Collection contents mismatch", errMessagePrefix),
                actual,
                Matchers.containsInAnyOrder(expected.toArray()));
        }
    }

    /**
     * Comparator for cases when collection contents check are not needed.
     */
    private static class CollectionsSizeOnlyComparator extends CollectionsComparator {
        @Override
        void compareContents(Collection<?> expected, Collection<?> actual, String errMessagePrefix) {
            // don't compare contents - they are null
        }
    }

}
