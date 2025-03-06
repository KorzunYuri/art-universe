package yurykorzun.art.universe.common.data.raw.task.entity;

import yurykorzun.art.universe.common.data.raw.Coded;

import java.time.Duration;

/**
 * Any data collection task type has to:
 *  1) be enum
 *  2) implement this interface
 *  3) register its values in {@link TaskTypeRegistry}
 *  <pre>
 *  {@code
 *      public enum MyTask implements dataCollectionTaskType {
 *          TASK_1("task_1"), TASK_2("task_2");
 *
 *          private final String code;
 *          MyTask(String code) {
 *              this.code = code;
 *          }
 *
 *          @Override
 *          public String getCode() {
 *              return code;
 *          }
 *
 *          static {
 *              Arrays.stream(values()).forEach(DataCollectionTaskTypeRegistry::register);
 *          }
 *
 *  }
 *  </pre>
 */
public interface TaskType extends Coded {
    Duration getDueDuration();
}
