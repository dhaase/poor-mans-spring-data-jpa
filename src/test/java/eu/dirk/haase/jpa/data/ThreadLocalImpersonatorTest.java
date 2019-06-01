package eu.dirk.haase.jpa.data;

import eu.dirk.haase.security.ImpersonationContext;
import eu.dirk.haase.security.Impersonator;
import eu.dirk.haase.security.ThreadLocalImpersonator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(BlockJUnit4ClassRunner.class)
public class ThreadLocalImpersonatorTest {

    @Test
    public void test_that_user_is_changed_in_a_sequence_of_contexts() {
        // Given
        Impersonator impersonator = new ThreadLocalImpersonator(() -> {
        });
        Supplier<String> currentUserSupplier = impersonator.currentUserSupplier();
        // When
        //     Do First change
        String beforeImpersonation1 = currentUserSupplier.get();
        ImpersonationContext context1 = impersonator.impersonate("user-1");
        String currUserInner1 = currentUserSupplier.get();
        context1.close();
        String afterImpersonation1 = currentUserSupplier.get();
        //     Do Second change
        String beforeImpersonation2 = currentUserSupplier.get();
        ImpersonationContext context2 = impersonator.impersonate("user-2");
        String currUserInner2 = currentUserSupplier.get();
        context2.close();
        String afterImpersonation2 = currentUserSupplier.get();
        //     Do Third change
        String beforeImpersonation3 = currentUserSupplier.get();
        ImpersonationContext context3 = impersonator.impersonate("user-3");
        String currUserInner3 = currentUserSupplier.get();
        context3.close();
        String afterImpersonation3 = currentUserSupplier.get();
        // Then
        //     Assert First change
        assertThat(beforeImpersonation1).isNull();
        assertThat(currUserInner1).isEqualTo("user-1");
        assertThat(afterImpersonation1).isNull();
        //     Assert Second change
        assertThat(beforeImpersonation2).isNull();
        assertThat(currUserInner2).isEqualTo("user-2");
        assertThat(afterImpersonation2).isNull();
        //     Assert Third change
        assertThat(beforeImpersonation3).isNull();
        assertThat(currUserInner3).isEqualTo("user-3");
        assertThat(afterImpersonation3).isNull();
    }

    @Test
    public void test_that_user_is_changed_in_a_sequence_of_runnable() {
        // Given
        final String[] currUserInner1 = {null};
        final String[] currUserInner2 = {null};
        final String[] currUserInner3 = {null};
        Impersonator impersonator = new ThreadLocalImpersonator(() -> {
        });
        Supplier<String> currentUserSupplier = impersonator.currentUserSupplier();
        // When
        //     Do First change
        String beforeImpersonation1 = currentUserSupplier.get();
        impersonator.impersonate("user-1", () -> {
            currUserInner1[0] = currentUserSupplier.get();
        });
        String afterImpersonation1 = currentUserSupplier.get();
        //     Do Second change
        String beforeImpersonation2 = currentUserSupplier.get();
        impersonator.impersonate("user-2", () -> {
            currUserInner2[0] = currentUserSupplier.get();
        });
        String afterImpersonation2 = currentUserSupplier.get();
        //     Do Third change
        String beforeImpersonation3 = currentUserSupplier.get();
        impersonator.impersonate("user-3", () -> {
            currUserInner3[0] = currentUserSupplier.get();
        });
        String afterImpersonation3 = currentUserSupplier.get();
        // Then
        //     Assert First change
        assertThat(beforeImpersonation1).isNull();
        assertThat(currUserInner1[0]).isEqualTo("user-1");
        assertThat(afterImpersonation1).isNull();
        //     Assert Second change
        assertThat(beforeImpersonation2).isNull();
        assertThat(currUserInner2[0]).isEqualTo("user-2");
        assertThat(afterImpersonation2).isNull();
        //     Assert Third change
        assertThat(beforeImpersonation3).isNull();
        assertThat(currUserInner3[0]).isEqualTo("user-3");
        assertThat(afterImpersonation3).isNull();
    }

    @Test
    public void test_that_user_is_changed_while_context_is_active() {
        // Given
        Impersonator impersonator = new ThreadLocalImpersonator(() -> {
        });
        Supplier<String> currentUserSupplier = impersonator.currentUserSupplier();
        // When
        String beforeImpersonation = currentUserSupplier.get();
        ImpersonationContext context = impersonator.impersonate("user-1");
        String currUserInner = currentUserSupplier.get();
        context.close();
        String afterImpersonation = currentUserSupplier.get();
        // Then
        assertThat(beforeImpersonation).isNull();
        assertThat(currUserInner).isEqualTo("user-1");
        assertThat(afterImpersonation).isNull();
    }

    @Test
    public void test_that_user_is_changed_while_nested_context_is_active() {
        // Given -----------------------------------
        Impersonator impersonator = new ThreadLocalImpersonator(() -> {
        });
        Supplier<String> currentUserSupplier = impersonator.currentUserSupplier();
        // When -----------------------------------
        String beforeImpersonation = currentUserSupplier.get();
        //    -> Enter Level One
        ImpersonationContext context1 = impersonator.impersonate("user-1");
        String currUserEnter1 = currentUserSupplier.get();
        String currUserInner1 = context1.currentUser();
        String lastUserInner1 = context1.lastUser();
        //        -> Enter Level Two
        ImpersonationContext context2 = impersonator.impersonate("user-2");
        String currUserEnter2 = currentUserSupplier.get();
        String currUserInner2 = context2.currentUser();
        String lastUserInner2 = context2.lastUser();
        //            -> Enter Level Three
        ImpersonationContext context3 = impersonator.impersonate("user-3");
        String currUserEnter3 = currentUserSupplier.get();
        String currUserInner3 = context3.currentUser();
        String lastUserInner3 = context3.lastUser();
        //            <- Leave Level Three
        context3.close();
        String currUserLeave3 = currentUserSupplier.get();
        //        <- Leave Level Two
        context2.close();
        String currUserLeave2 = currentUserSupplier.get();
        //    <- Leave Level One
        context1.close();
        String currUserLeave1 = currentUserSupplier.get();
        // Then -----------------------------------
        assertThat(beforeImpersonation).isNull();
        //    -> Enter Level One
        assertThat(currUserEnter1).isEqualTo("user-1");
        assertThat(currUserEnter1).isEqualTo(currUserInner1);
        assertThat(lastUserInner1).isNull();
        //        -> Enter Level Two
        assertThat(currUserEnter2).isEqualTo("user-2");
        assertThat(currUserEnter2).isEqualTo(currUserInner2);
        assertThat(lastUserInner2).isEqualTo("user-1");
        //            -> Enter Level Three
        assertThat(currUserEnter3).isEqualTo("user-3");
        assertThat(currUserEnter3).isEqualTo(currUserInner3);
        assertThat(lastUserInner3).isEqualTo("user-2");
        //            <- Leave Level Three
        assertThat(currUserLeave3).isEqualTo("user-2");
        //        <- Leave Level Two
        assertThat(currUserLeave2).isEqualTo("user-1");
        //    <- Leave Level One
        assertThat(currUserLeave1).isNull();
    }

    @Test
    public void test_that_user_is_changed_while_nested_runnable_is_active() {
        // Given
        final String[] currUserInner1 = {null, null};
        final String[] currUserInner2 = {null, null};
        final String[] currUserInner3 = {null};
        Impersonator impersonator = new ThreadLocalImpersonator(() -> {
        });
        Supplier<String> currentUserSupplier = impersonator.currentUserSupplier();
        // When
        String beforeImpersonation = currentUserSupplier.get();
        impersonator.impersonate("user-1", () -> {
            currUserInner1[0] = currentUserSupplier.get();
            impersonator.impersonate("user-2", () -> {
                currUserInner2[0] = currentUserSupplier.get();
                impersonator.impersonate("user-3", () -> {
                    currUserInner3[0] = currentUserSupplier.get();
                });
                currUserInner2[1] = currentUserSupplier.get();
            });
            currUserInner1[1] = currentUserSupplier.get();
        });
        String afterImpersonation = currentUserSupplier.get();
        // Then
        assertThat(beforeImpersonation).isNull();
        assertThat(currUserInner1[0]).isEqualTo("user-1");
        assertThat(currUserInner2[0]).isEqualTo("user-2");
        assertThat(currUserInner3[0]).isEqualTo("user-3");
        assertThat(currUserInner2[1]).isEqualTo("user-2");
        assertThat(currUserInner1[1]).isEqualTo("user-1");
        assertThat(afterImpersonation).isNull();
    }

    @Test
    public void test_that_user_is_changed_while_runnable_is_active() {
        // Given
        Impersonator impersonator = new ThreadLocalImpersonator(() -> {
        });
        Supplier<String> currentUserSupplier = impersonator.currentUserSupplier();
        // When
        String beforeImpersonation = currentUserSupplier.get();
        final String[] currUserInner = {null};
        impersonator.impersonate("user-1", () -> {
            currUserInner[0] = currentUserSupplier.get();
        });
        String afterImpersonation = currentUserSupplier.get();
        // Then
        assertThat(beforeImpersonation).isNull();
        assertThat(currUserInner[0]).isEqualTo("user-1");
        assertThat(afterImpersonation).isNull();
    }

    @Test
    public void test_that_user_is_restored_when_only_outer_context_is_closed() {
        // Given -----------------------------------
        Impersonator impersonator = new ThreadLocalImpersonator(() -> {
        });
        Supplier<String> currentUserSupplier = impersonator.currentUserSupplier();
        // When -----------------------------------
        String beforeImpersonation = currentUserSupplier.get();
        //    -> Enter Level One
        ImpersonationContext context1 = impersonator.impersonate("user-1");
        //        -> Enter Level Two
        impersonator.impersonate("user-2");
        //            -> Enter Level Three
        impersonator.impersonate("user-3");
        //    <- Leave Level One (leaving two and three implicitly)
        context1.close();
        String afterImpersonation = currentUserSupplier.get();
        // Then -----------------------------------
        assertThat(beforeImpersonation).isNull();
        assertThat(afterImpersonation).isNull();
    }

}
