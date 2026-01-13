package yurykorzun.art.universe.common;

import yurykorzun.art.universe.common.domain.converter.CodedConverter;

/**
 * <p>Interface  for enums persistence.</p>
 *
 * <p>Any {@link Coded} implementation has to:
 * <ol>
 *     <li>register its values in {@link CodedRegistry}
 * <pre>
 *  {@code
 *      public enum TaskStatus implements Coded {
 *
 *          private final int code;
 *
 *          TaskStatus(int code) {
 *              this.code = code;
 *          }
 *
 *          @Override
 *          public Integer getCode() {
 *              return code;
 *          }
 *
 *          static {
 *              CodedRegistry.register(Arrays.asList(values()), TaskStatus.class);
 *          }
 *      }
 *  }
 * </pre>
 *     </li>
 *     <li>
 *         have a dedicated extension of {@link CodedConverter} for persistence convertation.
 *         When used as an {@link jakarta.persistence.Entity} field, it must have {@link jakarta.persistence.Converter} on it.
 *         <pre>
 *             {@code
 *                  @Converter(autoApply = true)
 *                  public class TaskTypeConverter extends CodedConverter<TaskType> {
 *
 *                  public TaskTypeConverter() {
 *                      super(TaskType.class);
 *                  }
 *
 *              }
 *             }
 *         </pre>
 *         <pre>
 *             {@code
 *                  @NonNull
 *                  @Column(name = "task_type")
 *                  @Convert(converter = TaskTypeConverter.class)
 *                  private TaskType type;
 *             }
 *         </pre>
 *     </li>
 * </ol>
 * </p>
 */
public interface Coded {
    Integer getCode();
    String getName();
}
