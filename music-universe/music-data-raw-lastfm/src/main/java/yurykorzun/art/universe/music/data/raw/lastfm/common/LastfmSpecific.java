package yurykorzun.art.universe.music.data.raw.lastfm.common;

import yurykorzun.art.universe.common.data.raw.DataSourceSpecific;
import yurykorzun.art.universe.music.MusicDomainConstants;

public interface LastfmSpecific extends DataSourceSpecific {

    @Override
    default String getDomainCode() {
        return MusicDomainConstants.DOMAIN_CODE;
    };

    @Override
    default String getDataSourceCode() {
        return LastfmConstants.DATA_SOURCE_CODE;
    }
}
