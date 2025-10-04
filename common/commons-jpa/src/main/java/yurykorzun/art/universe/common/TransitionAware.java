package yurykorzun.art.universe.common;

public interface TransitionAware <T extends TransitionAware<T>>{
    boolean isValidTransition(T to);
}
