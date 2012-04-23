package edu.mit.mobile.android.demomode;

/*
 * Copyright (C) 2012  MIT Mobile Experience Lab
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License Version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import android.net.Uri;
import edu.mit.mobile.android.content.ContentItem;
import edu.mit.mobile.android.content.ProviderUtils;
import edu.mit.mobile.android.content.UriPath;
import edu.mit.mobile.android.content.column.DBColumn;
import edu.mit.mobile.android.content.column.TextColumn;

@UriPath(LauncherItem.PATH)
public class LauncherItem implements ContentItem {

	@DBColumn(type = TextColumn.class)
	public static final String PACKAGE_NAME = "package";

	@DBColumn(type = TextColumn.class)
	public static final String ACTIVITY_NAME = "activity";

	public static final String PATH = "apps";

	public static Uri CONTENT_URI = ProviderUtils.toContentUri(HomescreenProvider.AUTHORITY, PATH);
}

