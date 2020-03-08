package com.onarandombox.MultiverseCore;

import java.util.Map;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MultiverseCoreConfig;
import com.onarandombox.MultiverseCore.event.MVDebugModeEvent;
import me.main__.util.SerializationConfig.NoSuchPropertyException;
import me.main__.util.SerializationConfig.Property;
import me.main__.util.SerializationConfig.SerializationConfig;
import org.bukkit.Bukkit;

/**
 * Our configuration.
 */
public class MultiverseCoreConfiguration extends SerializationConfig implements MultiverseCoreConfig {
    private static MultiverseCoreConfiguration instance;

    public MultiverseCoreConfiguration(final Map<String, Object> values) {
        super(values);
        MultiverseCoreConfiguration.setInstance(this);
    }

    /**
     * @return True if the static instance of config is set.
     */
    public static boolean isSet() {
        return instance != null;
    }

    /**
     * Gets the statically saved instance.
     * @return The statically saved instance.
     */
    public static MultiverseCoreConfiguration getInstance() {
        if (instance == null)
            throw new IllegalStateException("The instance wasn't set!");
        return instance;
    }

    @Property
    private volatile boolean enforceaccess;
    @Property
    private volatile boolean teleportintercept;
    @Property
    private volatile boolean firstspawnoverride;
    @Property
    private volatile boolean displaypermerrors;
    @Property
    private volatile int globaldebug;
    @Property
    private volatile boolean silentstart;
    @Property
    private volatile int messagecooldown;
    @Property
    private volatile double version;
    @Property
    private volatile String firstspawnworld;
    @Property
    private volatile int teleportcooldown;
    @Property
    private volatile boolean defaultportalsearch;
    @Property
    private volatile int portalsearchradius;
    @Property
    private volatile boolean autopurge;
    @Property
    private volatile boolean idonotwanttodonate;

    public MultiverseCoreConfiguration() {
        super();
        MultiverseCoreConfiguration.setInstance(this);
    }

    /**
     * Sets the statically saved instance.
     *
     * @param instance The new instance.
     */
    public static void setInstance(final MultiverseCoreConfiguration instance) {
        MultiverseCoreConfiguration.instance = instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setDefaults() {
        // BEGIN CHECKSTYLE-SUPPRESSION: MagicNumberCheck
        enforceaccess       = false;
        teleportintercept   = true;
        firstspawnoverride  = true;
        displaypermerrors   = true;
        globaldebug         = 0;
        messagecooldown     = 5000;
        teleportcooldown    = 1000;
        version             = 2.9;
        silentstart         = false;
        defaultportalsearch = false;
        portalsearchradius  = 128;
        autopurge           = true;
        idonotwanttodonate  = false;
        // END CHECKSTYLE-SUPPRESSION: MagicNumberCheck
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setConfigProperty(final String property, final String value) {
        try {
            return setProperty(property, value, true);
        }
        catch (final NoSuchPropertyException e) {
            return false;
        }
    }

    // And here we go:

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getEnforceAccess() {
        return enforceaccess;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnforceAccess(final boolean enforceAccess) {
        enforceaccess = enforceAccess;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getTeleportIntercept() {
        return teleportintercept;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTeleportIntercept(final boolean teleportIntercept) {
        teleportintercept = teleportIntercept;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getFirstSpawnOverride() {
        return firstspawnoverride;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFirstSpawnOverride(final boolean firstSpawnOverride) {
        firstspawnoverride = firstSpawnOverride;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getDisplayPermErrors() {
        return displaypermerrors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisplayPermErrors(final boolean displayPermErrors) {
        displaypermerrors = displayPermErrors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGlobalDebug() {
        return globaldebug;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGlobalDebug(final int globalDebug) {
        globaldebug = globalDebug;
        Logging.setDebugLevel(globalDebug);
        Bukkit.getPluginManager().callEvent(new MVDebugModeEvent(globalDebug));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMessageCooldown() {
        return messagecooldown;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessageCooldown(final int messageCooldown) {
        messagecooldown = messageCooldown;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getVersion() {
        return version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVersion(final int version) {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFirstSpawnWorld() {
        return firstspawnworld;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFirstSpawnWorld(final String firstSpawnWorld) {
        firstspawnworld = firstSpawnWorld;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTeleportCooldown() {
        return teleportcooldown;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTeleportCooldown(final int teleportCooldown) {
        teleportcooldown = teleportCooldown;
    }

    @Override
    public void setSilentStart(final boolean silentStart) {
        Logging.setShowingConfig(!silentStart);
        silentstart = silentStart;
    }

    @Override
    public boolean getSilentStart() {
        return silentstart;
    }

    @Override
    public void setUseDefaultPortalSearch(final boolean useDefaultPortalSearch) {
        defaultportalsearch = useDefaultPortalSearch;
    }

    @Override
    public boolean isUsingDefaultPortalSearch() {
        return defaultportalsearch;
    }

    @Override
    public void setPortalSearchRadius(final int searchRadius) {
        portalsearchradius = searchRadius;
    }

    @Override
    public int getPortalSearchRadius() {
        return portalsearchradius;
    }

    @Override
    public boolean isAutoPurgeEnabled() {
        return autopurge;
    }

    @Override
    public void setAutoPurgeEnabled(final boolean autopurge) {
        this.autopurge = autopurge;
    }

    @Override
    public boolean isShowingDonateMessage() {
        return !idonotwanttodonate;
    }

    @Override
    public void setShowDonateMessage(final boolean showDonateMessage) {
        idonotwanttodonate = !showDonateMessage;
    }
}
