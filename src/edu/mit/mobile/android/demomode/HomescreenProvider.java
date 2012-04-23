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
import edu.mit.mobile.android.content.GenericDBHelper;
import edu.mit.mobile.android.content.SimpleContentProvider;

public class HomescreenProvider extends SimpleContentProvider {

	public static final String AUTHORITY = "edu.mit.mobile.android.demomode";

	private static final int DB_VERSION = 1;

	public HomescreenProvider() {
		super(AUTHORITY, DB_VERSION);
	}

	@Override
	public boolean onCreate() {
		super.onCreate();

		final GenericDBHelper items = new GenericDBHelper(LauncherItem.class);
		addDirAndItemUri(items, LauncherItem.PATH);

		return true;
	}
}
