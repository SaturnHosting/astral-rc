package dev.astral.rc;

import dev.astral.rc.commands.RCCommand;
import dev.astral.rc.modules.RCModule;
import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class AstralRC extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Astral RC");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Astral RC");

        // Modules
        Modules.get().add(new RCModule());

        // Commands
        Commands.add(new RCCommand());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "dev.astral.rc";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("SaturnHosting", "astral-rc");
    }
}
