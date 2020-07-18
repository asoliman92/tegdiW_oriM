package com.miro.platform.widget.domain;

import com.miro.platform.widget.domain.exceptions.ResourceNotFoundException;
import com.miro.platform.widget.domain.repository.WidgetCustomRepo;
import com.miro.platform.widget.domain.utils.IdGenerator;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.miro.platform.widget.Utils.getDummyBuilderNoIdNoZindex;

public class WidgetCustomRepoTest {
    private WidgetCustomRepo widgetRepo = new WidgetCustomRepo(new IdGenerator());

    @Nested
    public class WhenAddWidget {
        @Test
        public void givenWidgetWithoutZindex_ToEmptyPlane_ThenAppendedToListWithZeroZindex() {
            Widget toAdd = getDummyBuilderNoIdNoZindex().build();

            Widget addedWidget = widgetRepo.addWidget(toAdd);
            Assert.assertEquals("Wrong zIndex value", 0L, addedWidget.getzIndex().longValue());
            Assert.assertEquals("List size mismatch", 1, widgetRepo.findAllSorted().size());
            Assert.assertEquals("Widget not correct", addedWidget, widgetRepo.findAllSorted().get(0));
            Assert.assertEquals(Optional.of(addedWidget), widgetRepo.findByZindex(0L));
        }

        @Nested
        public class GivenWidgetCustomRepoWithExistingWidgets {
            //Make sure they are sorted, they will be sorted anyways inside
            //But for tests to run smoothly keep it sorted
            Long[] zIndexes = new Long[]{-5L, 0L, 5L};

            @BeforeEach
            public void setup() {
                List<Widget> widgetList = new ArrayList<>();
                for (long zIndex : zIndexes) {
                    Widget widget = getDummyBuilderNoIdNoZindex()
                            .withZIndex(zIndex)
                            .build();
                    widgetList.add(widget);
                }
                widgetRepo = new WidgetCustomRepo(new IdGenerator(), widgetList);
            }

            @Test
            public void givenWidgetWithoutZindex_ThenAppendedToListWithHigherIndex() {
                Widget toAdd = getDummyBuilderNoIdNoZindex()
                        .build();

                Widget addedWidget = widgetRepo.addWidget(toAdd);
                Assert.assertNotNull(addedWidget.getId());
                Assert.assertNotNull(addedWidget.getzIndex());
                Assert.assertEquals("Wrong zIndex value", 6L, addedWidget.getzIndex().longValue());
                List<Widget> widgetList = widgetRepo.findAllSorted();
                Assert.assertEquals("List size mismatch", 4, widgetList.size());
                Assert.assertEquals("Widget not correct", addedWidget, widgetList.get(widgetList.size() - 1));
                Assert.assertEquals(Optional.of(addedWidget), widgetRepo.findByZindex(addedWidget.getzIndex()));
                Assert.assertEquals(addedWidget.getzIndex().longValue(), widgetRepo.getForeground());
            }

            @Test
            public void givenWidgetWithExistingZindex_ThenAddToCorrectPositionAndShift() {
                long newZindex = 0L;
                Widget toAdd = getDummyBuilderNoIdNoZindex()
                        .withZIndex(newZindex)
                        .build();

                Widget addedWidget = widgetRepo.addWidget(toAdd);
                List<Widget> widgetList = widgetRepo.findAllSorted();
                int expectedWidgetIndex = 1;
                Assert.assertNotNull(addedWidget.getId());
                Assert.assertNotNull(addedWidget.getzIndex());
                Assert.assertEquals("List size mismatch", 4, widgetList.size());
                Assert.assertEquals("Widget not in the right place", addedWidget, widgetList.get(expectedWidgetIndex));
                Assert.assertEquals("Wrong zIndex value", zIndexes[0], widgetList.get(0).getzIndex());
                Assert.assertEquals(6L, widgetRepo.getForeground());
                for (int i = expectedWidgetIndex + 1, j = 1; i < widgetList.size(); i++, j++) {
                    Long expectedZindex = zIndexes[j] + 1;
                    Widget tmpWidget = widgetList.get(i);
                    Assert.assertEquals("Wrong zIndex value", expectedZindex, tmpWidget.getzIndex());
                    Assert.assertEquals("zIndexMap keys mismatch",
                            widgetRepo.findByZindex(expectedZindex),
                            Optional.of(tmpWidget)
                    );
                }
            }

            @Test
            public void givenWidgetWithMinExistingZindex_ThenAddToCorrectPositionAndShiftAll() {
                long newZindex = -5L;
                Widget toAdd = getDummyBuilderNoIdNoZindex()
                        .withZIndex(newZindex)
                        .build();

                Widget addedWidget = widgetRepo.addWidget(toAdd);
                List<Widget> widgetList = widgetRepo.findAllSorted();
                int expectedWidgetIndex = 0;
                Assert.assertEquals("List size mismatch", 4, widgetList.size());
                Assert.assertEquals("Widget not in the right place", addedWidget, widgetList.get(expectedWidgetIndex));
                Assert.assertEquals("Wrong zIndex value", zIndexes[0], widgetList.get(0).getzIndex());
                Assert.assertEquals(6L, widgetRepo.getForeground());
                Assert.assertNotNull(addedWidget.getId());
                Assert.assertNotNull(addedWidget.getzIndex());
                for (int i = expectedWidgetIndex + 1, j = 0; i < widgetList.size(); i++, j++) {
                    Long expectedZindex = zIndexes[j] + 1;
                    Widget tmpWidget = widgetList.get(i);
                    Assert.assertEquals("Wrong zIndex value", expectedZindex, tmpWidget.getzIndex());
                    Assert.assertEquals("zIndexMap keys mismatch",
                            widgetRepo.findByZindex(expectedZindex),
                            Optional.of(tmpWidget)
                    );
                }
            }


            @Test
            public void givenWidgetWithNonExistingZindex_ThenAddInCorrectPositionWithoutShifting() {
                long newZindex = 3;
                Widget toAdd = getDummyBuilderNoIdNoZindex()
                        .withZIndex(newZindex)
                        .build();
                Widget addedWidget = widgetRepo.addWidget(toAdd);
                List<Widget> widgetList = widgetRepo.findAllSorted();
                int expectedWidgetIndex = 2;
                Assert.assertNotNull(addedWidget.getId());
                Assert.assertNotNull(addedWidget.getzIndex());
                Assert.assertEquals("List size mismatch", 4, widgetList.size());
                Assert.assertEquals("Widget not in the right place", addedWidget, widgetList.get(expectedWidgetIndex));
                Assert.assertEquals(5L, widgetRepo.getForeground());
                Assert.assertEquals(5L, widgetList.get(widgetList.size() - 1).getzIndex().longValue());
            }
        }
    }

    @Nested
    public class WhenRemoveWidget {
        //Make sure they are sorted, they will be sorted anyways inside
        //But for tests to run smoothly keep it sorted
        Long[] zIndexes = new Long[]{-5L, 3L, 5L};

        @BeforeEach
        public void setup() {
            List<Widget> widgetList = new ArrayList<>();
            long i = 0;
            for (long zIndex : zIndexes) {
                Widget widget = getDummyBuilderNoIdNoZindex()
                        .withId(i++)
                        .withZIndex(zIndex)
                        .build();
                widgetList.add(widget);
            }
            widgetRepo = new WidgetCustomRepo(new IdGenerator(), widgetList);
        }

        @Test
        public void whenRemoveExistingId_ThenRemoveFromAllUnderlyingStructures() {
            int listIndex = 1;
            Long zIndex = zIndexes[listIndex];
            long id = widgetRepo.findAllSorted().get(listIndex).getId();

            widgetRepo.removeWidget(id);
            Assert.assertEquals(2, widgetRepo.findAllSorted().size());
            Assert.assertEquals(Optional.empty(), widgetRepo.findById(id));
            Assert.assertEquals(Optional.empty(), widgetRepo.findByZindex(zIndex));
            Assert.assertEquals(5L, widgetRepo.getForeground());
        }

        @Test
        public void whenRemoveNonExistingId_ThenThrowException() {
            Assert.assertThrows(ResourceNotFoundException.class,
                    () -> widgetRepo.removeWidget(299L));
        }

        @Test
        public void whenRemoveExistingMaxZIndexId_ThenForegroundEqPrevWidgetZindex() {
            int lastIndex = widgetRepo.findAllSorted().size() - 1;
            long id = widgetRepo.findAllSorted().get(lastIndex).getId();
            long zIndex = zIndexes[lastIndex - 1];
            widgetRepo.removeWidget(id);
            Assert.assertEquals(zIndex, widgetRepo.getForeground());
        }

        @Test
        public void whenRemoveAllWidgets_ThenResetForeground() {
            List<Long> idList = widgetRepo.findAllSorted().stream().map(Widget::getId).collect(Collectors.toList());
            for (Long id : idList) {
                widgetRepo.removeWidget(id);
            }

            Assert.assertEquals(-1L, widgetRepo.getForeground());
        }


    }

    @Nested
    public class WhenUpdateWidget {
        //Make sure they are sorted, they will be sorted anyways inside
        //But for tests to run smoothly keep it sorted
        Long[] zIndexes = new Long[]{-5L, 0L, 5L};

        @BeforeEach
        public void setup() {
            List<Widget> widgetList = new ArrayList<>();
            long i = 0;
            for (long zIndex : zIndexes) {
                Widget widget = getDummyBuilderNoIdNoZindex()
                        .withId(i++)
                        .withZIndex(zIndex)
                        .build();
                widgetList.add(widget);
            }
            widgetRepo = new WidgetCustomRepo(new IdGenerator(), widgetList);
        }

        @Test
        public void whenSameZindex_ThenChangeAttributes() {
            Long newAttribute = 10L;
            Long id = 1L;
            Widget widget = getDummyBuilderNoIdNoZindex(newAttribute)
                    .withZIndex(zIndexes[id.intValue()])
                    .withId(id)
                    .build();

            Widget updatedWidget = widgetRepo.updateWidget(widget);
            Assert.assertEquals(id, updatedWidget.getId());
            Assert.assertEquals(newAttribute, updatedWidget.getX());
            Assert.assertEquals(newAttribute, updatedWidget.getY());
            Assert.assertEquals(newAttribute, updatedWidget.getWidth());
            Assert.assertEquals(newAttribute, updatedWidget.getHeight());
            Assert.assertEquals(Optional.of(updatedWidget), widgetRepo.findById(id));
        }

        @Test
        public void whenChangeZindex_ThenRemoveAndAddNewWithSameId() {
            Long newAttribute = 10L;
            Long id = 1L;
            Long oldZindex = widgetRepo.findById(id).get().getzIndex();

            Widget widget = getDummyBuilderNoIdNoZindex(newAttribute)
                    .withZIndex(newAttribute)
                    .withId(id)
                    .build();

            Widget updatedWidget = widgetRepo.updateWidget(widget);
            //Nothing new added
            Assert.assertEquals(3, widgetRepo.findAllSorted().size());
            //No widget with old zIndex
            Assert.assertEquals(Optional.empty(), widgetRepo.findByZindex(oldZindex));
            Assert.assertEquals(Optional.of(updatedWidget), widgetRepo.findByZindex(newAttribute));
        }

        @Test
        public void whenNonExistentId_ThenThrowException() {
            Widget widget = getDummyBuilderNoIdNoZindex().withId(100L).build();
            Assert.assertThrows(ResourceNotFoundException.class, () -> widgetRepo.updateWidget(widget));
        }

//        @Test
//        public void whenWidgetIdNotSameAsChangedId_ThenThrowException() {
//            Widget widget = getDummyBuilderNoIdNoZindex().withId(100L).build();
//            Assert.assertThrows(IllegalChangeException.class, () -> widgetRepo.updateWidget(1L, widget));
//        }

    }

    @Nested
    public class WithConcurrency {

        @BeforeEach
        public void setup() {
            widgetRepo = new WidgetCustomRepo(new IdGenerator());
        }

        @Test
        public void addWidgets() throws InterruptedException {
            int numberOfThreads = 20;
            int numberOfOperations = 1550;
            ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfOperations);
            for (int i = 0; i < numberOfOperations; i++) {
                Widget widget = getDummyBuilderNoIdNoZindex().withZIndex(0L).build();
                service.execute(() -> {
                    widgetRepo.addWidget(widget);
                    latch.countDown();
                });
            }

            latch.await();
            List<Widget> widgetList = widgetRepo.findAllSorted();
            int listSize = widgetList.size();
            Assert.assertEquals(numberOfOperations-1, widgetList.get(listSize-1).getzIndex().longValue());
            Assert.assertEquals(numberOfOperations, listSize);
        }

        @Test
        public void updateWhileAdding() throws InterruptedException {
            int numberOfThreads = 20;
            int numberOfOperations = 1550;
            AtomicLong updates = new AtomicLong(0L);
            ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfOperations);
            for (int i = 1; i <= numberOfOperations; i++) {
                if (i % 10 == 0) {
                    Widget widget =
                            getDummyBuilderNoIdNoZindex()
                                    .withId(1L)
                                    .withZIndex(updates.getAndIncrement()).build();
                    service.execute(() -> {
                        widgetRepo.updateWidget(widget);
                        latch.countDown();
                    });
                    continue;
                }

                Widget widget = getDummyBuilderNoIdNoZindex().withZIndex(0L).build();
                service.execute(() -> {
                    widgetRepo.addWidget(widget);
                    latch.countDown();
                });
            }

            latch.await();
            List<Widget> widgetList = widgetRepo.findAllSorted();
            int listSize = widgetList.size();
            //Total widgets is totalOperations - number of updates
            Assert.assertEquals(numberOfOperations-updates.get(), listSize);
        }
    }
}
