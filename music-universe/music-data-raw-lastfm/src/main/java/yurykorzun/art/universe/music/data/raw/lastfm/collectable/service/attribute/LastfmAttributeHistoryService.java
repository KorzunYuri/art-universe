package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeHistoryRecord;

import javax.annotation.Nullable;
import java.util.List;

public interface LastfmAttributeHistoryService {

    /**
     * <p>Update attribute values following the rules:
     * <ol>
     *     <li>if attribute value doesn't exist - insert new record</li>
     *     <li>if attribute value exists:
     *          <ul>
     *              <li>if value has changed - expire old value and insert new record</li>
     *              <li>if attribute has 'snapshot' type and existing value is bound to different snapshot
     *                  - expire old value and insert new record</li>
     *              <li>if not - do nothing</li>
     *          </ul>
     *     </li>
     * </ol>
     * </p>
     * @param values candidates for new <i>current</i> values
     */
    void upsertCandidateValues(List<LastfmAttributeHistoryRecord> values);
}
