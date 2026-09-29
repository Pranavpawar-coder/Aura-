package com.example.aura.domain.model

enum class SortCriterion(val displayName: String) {
    TITLE("Title"),
    ARTIST("Artist"),
    ALBUM("Album"),
    ALBUM_ARTIST("Album Artist"),
    GENRE("Genre"),
    YEAR("Year"),
    DURATION("Duration"),
    DATE_ADDED("Date Added"),
    LAST_PLAYED("Last Played"),
    PLAY_COUNT("Play Count"),
    FILE_SIZE("File Size")
}

enum class SortDirection(val displayName: String) {
    ASCENDING("Ascending"),
    DESCENDING("Descending")
}

data class SortOption(
    val criterion: SortCriterion = SortCriterion.TITLE,
    val direction: SortDirection = SortDirection.ASCENDING
) {
    val displayName: String
        get() = when (direction) {
            SortDirection.ASCENDING -> when (criterion) {
                SortCriterion.TITLE, SortCriterion.ARTIST, SortCriterion.ALBUM,
                SortCriterion.ALBUM_ARTIST, SortCriterion.GENRE -> "${criterion.displayName} (A → Z)"
                SortCriterion.YEAR, SortCriterion.DATE_ADDED, SortCriterion.LAST_PLAYED -> "${criterion.displayName} (Oldest First)"
                SortCriterion.DURATION -> "${criterion.displayName} (Shortest First)"
                SortCriterion.PLAY_COUNT, SortCriterion.FILE_SIZE -> "${criterion.displayName} (Lowest First)"
            }
            SortDirection.DESCENDING -> when (criterion) {
                SortCriterion.TITLE, SortCriterion.ARTIST, SortCriterion.ALBUM,
                SortCriterion.ALBUM_ARTIST, SortCriterion.GENRE -> "${criterion.displayName} (Z → A)"
                SortCriterion.YEAR, SortCriterion.DATE_ADDED, SortCriterion.LAST_PLAYED -> "${criterion.displayName} (Newest First)"
                SortCriterion.DURATION -> "${criterion.displayName} (Longest First)"
                SortCriterion.PLAY_COUNT, SortCriterion.FILE_SIZE -> "${criterion.displayName} (Highest First)"
            }
        }
}

/**
 * Predefined sort orders matching standard library views and DataStore storage.
 */
enum class SongSortOrder(
    val displayName: String,
    val criterion: SortCriterion,
    val direction: SortDirection
) {
    TITLE_AZ("Title (A → Z)", SortCriterion.TITLE, SortDirection.ASCENDING),
    TITLE_ZA("Title (Z → A)", SortCriterion.TITLE, SortDirection.DESCENDING),
    ARTIST_AZ("Artist (A → Z)", SortCriterion.ARTIST, SortDirection.ASCENDING),
    ARTIST_ZA("Artist (Z → A)", SortCriterion.ARTIST, SortDirection.DESCENDING),
    ALBUM_AZ("Album (A → Z)", SortCriterion.ALBUM, SortDirection.ASCENDING),
    ALBUM_ZA("Album (Z → A)", SortCriterion.ALBUM, SortDirection.DESCENDING),
    DATE_ADDED("Date Added (Newest)", SortCriterion.DATE_ADDED, SortDirection.DESCENDING),
    DATE_ADDED_OLD("Date Added (Oldest)", SortCriterion.DATE_ADDED, SortDirection.ASCENDING),
    DURATION_DESC("Duration (Longest)", SortCriterion.DURATION, SortDirection.DESCENDING),
    DURATION_ASC("Duration (Shortest)", SortCriterion.DURATION, SortDirection.ASCENDING),
    YEAR_DESC("Year (Newest)", SortCriterion.YEAR, SortDirection.DESCENDING),
    YEAR_ASC("Year (Oldest)", SortCriterion.YEAR, SortDirection.ASCENDING),
    LAST_PLAYED("Last Played", SortCriterion.LAST_PLAYED, SortDirection.DESCENDING),
    MOST_PLAYED("Most Played", SortCriterion.PLAY_COUNT, SortDirection.DESCENDING),
    FILE_SIZE_DESC("File Size (Largest)", SortCriterion.FILE_SIZE, SortDirection.DESCENDING);

    companion object {
        fun fromName(name: String?): SongSortOrder {
            return values().firstOrNull { it.name == name } ?: TITLE_AZ
        }
    }
}
