package dev.jab125.earlyloadingscreen.fancymenu.v2;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class Plugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {

	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return FabricLoader.getInstance().getModContainer("fancymenu").map(a -> a.getMetadata().getVersion().compareTo(parse("2.0.0")) >= 0 && a.getMetadata().getVersion().compareTo(parse("3.0.0")) < 0).orElse(false);
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	static Version parse(String string) {
		return s(() -> Version.parse(string));
	}

	private interface G<T> {
		T get() throws Throwable;
		default T getSafe() {
			try {
				return get();
			} catch (Throwable e) {
				n(e);
			}
			return null;
		}
	}

	private static  <T extends Throwable> void n(Throwable t) throws T {
		throw (T) t;
	}

	private static  <T> T s(G<T> t) {
		return t.getSafe();
	}
}
