package de.keksuccino.fancymenu.util.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Objects;
import java.util.function.Supplier;

/** Keeps the render thread's transform state directly; other threads retain separate slots. */
final class RenderThreadLocal<T> extends ThreadLocal<T> {
    private final Supplier<? extends T> initial;
    private T renderValue;
    private boolean renderHeld;

    private RenderThreadLocal(Supplier<? extends T> initial) {
        this.initial = Objects.requireNonNull(initial);
    }

    public static <S> ThreadLocal<S> withInitial(Supplier<? extends S> initial) {
        return new RenderThreadLocal<>(initial);
    }

    @Override
    protected T initialValue() {
        return this.initial.get();
    }

    @Override
    public T get() {
        if (RenderSystem.isOnRenderThread()) {
            if (!this.renderHeld) {
                this.renderValue = super.get();
                this.renderHeld = true;
            }
            return this.renderValue;
        }
        return super.get();
    }

    @Override
    public void set(T value) {
        if (RenderSystem.isOnRenderThread()) {
            this.renderValue = value;
            this.renderHeld = true;
            return;
        }
        super.set(value);
    }

    @Override
    public void remove() {
        if (RenderSystem.isOnRenderThread()) {
            this.renderValue = null;
            this.renderHeld = false;
        }
        super.remove();
    }
}
