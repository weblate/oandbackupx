package com.machiav3lli.backup;

import android.content.SharedPreferences;
import android.util.SparseIntArray;

import com.machiav3lli.backup.adapters.MainAdapter;

import java.util.Comparator;

public class MainSorter {
    MainAdapter adapter;
    FilteringMethod filteringMethod = FilteringMethod.ALL;
    SortingMethod sortingMethod = SortingMethod.PACKAGENAME;
    // SparseIntArray is more memory efficient than mapping integers to integers using a hashmap
    private static final SparseIntArray convertFilteringIdMap, convertSortingIdMap;
    SharedPreferences.Editor prefsEdit;
    int oldBackups = 0;

    public MainSorter(MainAdapter adapter, SharedPreferences prefs) {
        this.adapter = adapter;
        this.prefsEdit = prefs.edit();
        try {
            oldBackups = Integer.parseInt(prefs.getString(
                    Constants.PREFS_OLDBACKUPS, "0"));
        } catch (NumberFormatException ignored) {
        }
    }

    static {
        convertFilteringIdMap = new SparseIntArray(3);
        convertFilteringIdMap.put(FilteringMethod.ALL.ordinal(), R.id.showAll);
        convertFilteringIdMap.put(FilteringMethod.SYSTEM.ordinal(), R.id.showOnlySystem);
        convertFilteringIdMap.put(FilteringMethod.USER.ordinal(), R.id.showOnlyUser);
    }

    static {
        convertSortingIdMap = new SparseIntArray(2);
        convertSortingIdMap.put(SortingMethod.LABEL.ordinal(), R.id.sortByLabel);
        convertSortingIdMap.put(SortingMethod.PACKAGENAME.ordinal(), R.id.sortByPackageName);
    }


    public enum FilteringMethod {
        ALL(R.id.showAll),
        SYSTEM(R.id.showOnlySystem),
        USER(R.id.showOnlyUser),
        NOTBACKEDUP(R.id.showNotBackedup),
        NOTINSTALLED(R.id.showNotInstalled),
        NEWANDUPDATED(R.id.showNewAndUpdated),
        OLDBACKUPS(R.id.showOldBackups),
        ONLYAPK(R.id.showOnlyApkBackedUp),
        ONLYDATA(R.id.showOnlyDataBackedUp),
        ONLYSPECIAL(R.id.showOnlySpecialBackups);
        int id;

        FilteringMethod(int id) {
            this.id = id;
        }

        int getId() {
            return id;
        }
    }

    public enum SortingMethod {
        LABEL(R.id.sortByLabel),
        PACKAGENAME(R.id.sortByPackageName);
        int id;

        SortingMethod(int id) {
            this.id = id;
        }

        int getId() {
            return id;
        }
    }

    public static Comparator<AppInfo> appInfoLabelComparator = (m1, m2) ->
            m1.getLabel().compareToIgnoreCase(m2.getLabel());
    public static Comparator<AppInfo> appInfoPackageNameComparator = (m1, m2) ->
            m1.getPackageName().compareToIgnoreCase(m2.getPackageName());

    public void sort(int id) {
        switch (id) {
            case R.id.showAll:
                filterShowAll();
                saveInPrefs("filteringId", filteringMethod.ordinal());
                break;
            case R.id.showOnlySystem:
                filteringMethod = FilteringMethod.SYSTEM;
                adapter.filterAppType(2);
                saveInPrefs("filteringId", filteringMethod.ordinal());
                break;
            case R.id.showOnlyUser:
                filteringMethod = FilteringMethod.USER;
                adapter.filterAppType(1);
                saveInPrefs("filteringId", filteringMethod.ordinal());
                break;
            case R.id.showNotBackedup:
                filteringMethod = FilteringMethod.NOTBACKEDUP;
                adapter.filterIsBackedup();
                prefsEdit.commit();
                break;
            case R.id.showNotInstalled:
                filteringMethod = FilteringMethod.NOTINSTALLED;
                adapter.filterIsInstalled();
                prefsEdit.commit();
                break;
            case R.id.showNewAndUpdated:
                filteringMethod = FilteringMethod.NEWANDUPDATED;
                adapter.filterNewAndUpdated();
                prefsEdit.commit();
                break;
            case R.id.showOldBackups:
                filteringMethod = FilteringMethod.OLDBACKUPS;
                adapter.filterOldApps(oldBackups);
                prefsEdit.commit();
                break;
            case R.id.showOnlyApkBackedUp:
                filteringMethod = FilteringMethod.ONLYAPK;
                adapter.filterPartialBackups(AppInfo.MODE_APK);
                prefsEdit.commit();
                break;
            case R.id.showOnlyDataBackedUp:
                filteringMethod = FilteringMethod.ONLYDATA;
                adapter.filterPartialBackups(AppInfo.MODE_DATA);
                prefsEdit.commit();
                break;
            case R.id.showOnlySpecialBackups:
                filteringMethod = FilteringMethod.ONLYSPECIAL;
                adapter.filterSpecialBackups();
                prefsEdit.commit();
                break;
            case R.id.sortByLabel:
                sortingMethod = SortingMethod.LABEL;
                adapter.sortByLabel();
                sort(filteringMethod.getId());
                saveInPrefs("sortingId", sortingMethod.ordinal());
                break;
            case R.id.sortByPackageName:
                sortingMethod = SortingMethod.PACKAGENAME;
                adapter.sortByPackageName();
                sort(filteringMethod.getId());
                saveInPrefs("sortingId", sortingMethod.ordinal());
                break;
        }
    }

    public void filterShowAll() {
        filteringMethod = FilteringMethod.ALL;
        adapter.filterAppType(0);
    }

    public FilteringMethod getFilteringMethod() {
        if (filteringMethod != null) {
            return filteringMethod;
        }
        return FilteringMethod.ALL;
    }

    public SortingMethod getSortingMethod() {
        if (sortingMethod != null) {
            return sortingMethod;
        }
        return SortingMethod.PACKAGENAME;
    }

    public void saveInPrefs(String prefName, int persistentId) {
        prefsEdit.putInt(prefName, persistentId);
        prefsEdit.commit();
    }

    // needs to be static as it is used in onPrepareOptionsMenu which is called before the sorter instance is created
    public static int convertFilteringId(int key) {
        // SparseIntArray returns 0 if the key is not found
        return convertFilteringIdMap.get(key);
    }

    public static int convertSortingId(int key) {
        return convertSortingIdMap.get(key);
    }
}