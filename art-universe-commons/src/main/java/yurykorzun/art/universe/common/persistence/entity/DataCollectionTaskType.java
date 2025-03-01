package yurykorzun.art.universe.common.persistence.entity;

/**
 * Any data collection task type has to:
 *  1) be enum
 *  2) implement this interface
 *  3) register its values in {@link DataCollectionTaskTypeRegistry}
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
public interface DataCollectionTaskType {
    String getCode();
}
