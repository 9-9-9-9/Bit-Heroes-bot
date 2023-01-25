package bh.bot.app.dev;

import bh.bot.app.AbstractApplication;
import bh.bot.app.AfkApp;
import bh.bot.app.ChangeCharacterApp;
import bh.bot.app.FishingApp;
import bh.bot.app.ReRunApp;
import bh.bot.common.exceptions.InvalidDataException;
import bh.bot.common.types.annotations.AppMeta;
import bh.bot.common.types.annotations.FlagMeta;
import bh.bot.common.types.flags.FlagPattern;
import bh.bot.common.types.flags.Flags;
import bh.bot.common.utils.StringUtil;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("DeprecatedIsStillUsed")
@AppMeta(code = "gen-meta", name = "Generate meta", requireClientType = false, dev = true, displayOrder = 100)
@Deprecated
public class GenerateMetaApp extends AbstractApplication {
    @Override
    protected void internalRun(String[] args) {
        List<AbstractApplication> apps = Arrays.asList(
                new ReRunApp(), //
                new FishingApp(), //
                new ChangeCharacterApp(), //
                new AfkApp() //
        );

        List<FlagInfo> flagInfos = Arrays.stream(Flags.allFlags).map(f -> {
            FlagMeta anFlagMeta = f.getClass().getAnnotation(FlagMeta.class);
            if (anFlagMeta == null)
                return null;

            FlagInfo flagInfo = new FlagInfo();
            flagInfo.code = f.getName();
            flagInfo.desc = StringUtil.firstNotBlank(anFlagMeta.cbDesc(), f.getDescription());
            flagInfo.val = StringUtil.firstNotBlank(anFlagMeta.cbVal());
            flagInfo.checked = anFlagMeta.checked();
            flagInfo.order = anFlagMeta.displayOrder();

            return flagInfo;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        //noinspection rawtypes
        List<FlagPattern> supportedFlags = Arrays.stream(Flags.allFlags).filter(f -> flagInfos.stream().anyMatch(fi -> fi.code.equals(f.getName()))).collect(Collectors.toList());

        List<AppInfo> appInfos = apps.stream().map(x -> {
            Class<? extends AbstractApplication> clz = x.getClass();
            AppMeta anAppMeta = clz.getAnnotation(AppMeta.class);

            AppInfo appInfo = new AppInfo();
            appInfo.name = anAppMeta.name();
            appInfo.code = anAppMeta.code();
            appInfo.flags = supportedFlags.stream().filter(f -> f.isSupportedByApp(x)).map(FlagPattern::getName).collect(Collectors.toList());
            appInfo.argType = anAppMeta.argType();
            appInfo.argAsk = anAppMeta.argAsk();
            appInfo.argDefault = anAppMeta.argDefault();
            appInfo.argRequired = anAppMeta.argRequired();
            appInfo.argHint = x.getArgHint();

            if (StringUtil.isBlank(appInfo.argType))
                throw new InvalidDataException("Arg type is required in @AppMeta");

            if (StringUtil.isBlank(appInfo.argDefault) && !appInfo.argType.equals("none"))
                throw new InvalidDataException("Arg default value is required in @AppMeta");

            if (StringUtil.isBlank(appInfo.argAsk) && !appInfo.argType.equals("none"))
                throw new InvalidDataException("Arg asking is required in @AppMeta");

            return appInfo;
        }).collect(Collectors.toList());

        Meta meta = new Meta();
        meta.apps = appInfos;
        meta.flags = flagInfos;

        String json = new JSONObject(meta).toString();

        try {
            Files.write(Paths.get("./web/json/meta.json"), json.getBytes());
            System.out.println(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String getUsage() {
        return null;
    }

    @Override
    protected String getDescription() {
        return "(developers only) Generate meta app";
    }

    @Override
    protected String getLimitationExplain() {
        return "developers only";
    }

    @SuppressWarnings("unused")
    public static class Meta {
        private List<AppInfo> apps;
        private List<FlagInfo> flags;

        public List<AppInfo> getApps() {
            return apps;
        }

        public void setApps(List<AppInfo> apps) {
            this.apps = apps;
        }

        public List<FlagInfo> getFlags() {
            return flags;
        }

        public void setFlags(List<FlagInfo> flags) {
            this.flags = flags;
        }
    }

    @SuppressWarnings("unused")
    public static class AppInfo {
        private String name;
        private String code;
        private List<String> flags;
        private String argType;
        private String argAsk;
        private String argDefault;
        private boolean argRequired;
        private String argHint;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public List<String> getFlags() {
            return flags;
        }

        public void setFlags(List<String> flags) {
            this.flags = flags;
        }

        public String getArgType() {
            return argType;
        }

        public void setArgType(String argType) {
            this.argType = argType;
        }

        public String getArgDefault() {
            return argDefault;
        }

        public void setArgDefault(String argDefault) {
            this.argDefault = argDefault;
        }

        public String getArgAsk() {
            return argAsk;
        }

        public void setArgAsk(String argAsk) {
            this.argAsk = argAsk;
        }

        public boolean isArgRequired() {
            return argRequired;
        }

        public void setArgRequired(boolean argRequired) {
            this.argRequired = argRequired;
        }

        public String getArgHint() {
            return argHint;
        }

        public void setArgHint(String argHint) {
            this.argHint = argHint;
        }
    }

    public static class FlagInfo {
        private String code;
        private String desc;
        private String val;
        private boolean checked;
        private double order;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getVal() {
            return val;
        }

        public void setVal(String val) {
            this.val = val;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public double getOrder() {
            return order;
        }

        public void setOrder(double order) {
            this.order = order;
        }
    }
}
