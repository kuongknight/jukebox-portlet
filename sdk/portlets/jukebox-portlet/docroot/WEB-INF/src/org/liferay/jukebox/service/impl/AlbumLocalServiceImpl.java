/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package org.liferay.jukebox.service.impl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ServiceContext;
import org.liferay.jukebox.AlbumNameException;
import org.liferay.jukebox.model.Album;
import org.liferay.jukebox.service.base.AlbumLocalServiceBaseImpl;

import java.util.Date;
import java.util.List;

/**
 * The implementation of the album local service.
 *
 * <p>
 * All custom service methods should be put in this class. Whenever methods are added, rerun ServiceBuilder to copy their definitions into the {@link org.liferay.jukebox.service.AlbumLocalService} interface.
 *
 * <p>
 * This is a local service. Methods of this service will not have security checks based on the propagated JAAS credentials because this service can only be accessed from within the same VM.
 * </p>
 *
 * @author Julio Camarero
 * @see org.liferay.jukebox.service.base.AlbumLocalServiceBaseImpl
 * @see org.liferay.jukebox.service.AlbumLocalServiceUtil
 */
public class AlbumLocalServiceImpl extends AlbumLocalServiceBaseImpl {

	public Album addAlbum(
			long userId, long artistId, String name, int year,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(userId);

		Date now = new Date();

		validate(name);

		long albumId = counterLocalService.increment(Album.class.getName());

		Album album = albumPersistence.create(albumId);

		album.setUuid(serviceContext.getUuid());
		album.setGroupId(serviceContext.getScopeGroupId());
		album.setCompanyId(user.getCompanyId());
		album.setUserId(user.getUserId());
		album.setUserName(user.getFullName());
		album.setCreateDate(serviceContext.getCreateDate(now));
		album.setModifiedDate(serviceContext.getModifiedDate(now));
		album.setArtistId(artistId);
		album.setName(name);
		album.setYear(year);
		album.setExpandoBridgeAttributes(serviceContext);

		albumPersistence.update(album);

		// Resources

		if (serviceContext.isAddGroupPermissions() ||
			serviceContext.isAddGuestPermissions()) {

			addEntryResources(
				album, serviceContext.isAddGroupPermissions(),
				serviceContext.isAddGuestPermissions());
		}
		else {
			addEntryResources(
				album, serviceContext.getGroupPermissions(),
				serviceContext.getGuestPermissions());
		}

		// Asset

		updateAsset(
			userId, album, serviceContext.getAssetCategoryIds(),
			serviceContext.getAssetTagNames());

		return album;
	}

	@Override
	public void addEntryResources(
			Album album, boolean addGroupPermissions,
			boolean addGuestPermissions)
		throws PortalException, SystemException {

		resourceLocalService.addResources(
			album.getCompanyId(), album.getGroupId(), album.getUserId(),
			Album.class.getName(), album.getAlbumId(), false,
			addGroupPermissions, addGuestPermissions);
	}

	@Override
	public void addEntryResources(
			Album album, String[] groupPermissions, String[] guestPermissions)
		throws PortalException, SystemException {

		resourceLocalService.addModelResources(
			album.getCompanyId(), album.getGroupId(), album.getUserId(),
			Album.class.getName(), album.getAlbumId(), groupPermissions,
			guestPermissions);
	}

	public List<Album> getAlbums(long groupId, int start, int end)
		throws SystemException {

		return albumPersistence.findByGroupId(groupId, start, end);
	}

	public List<Album> getAlbums(long groupId) throws SystemException {
		return albumPersistence.findByGroupId(groupId);
	}

	public int getAlbumsCount(long groupId) throws SystemException {
		return albumPersistence.countByGroupId(groupId);
	}

	public Album updateAlbum(
			long userId, long albumId, long artistId, String name, int year,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		// Event

		User user = userPersistence.findByPrimaryKey(userId);

		validate(name);

		Album album = albumPersistence.findByPrimaryKey(albumId);

		album.setModifiedDate(serviceContext.getModifiedDate(null));
		album.setArtistId(artistId);
		album.setName(name);
		album.setYear(year);
		album.setExpandoBridgeAttributes(serviceContext);

		albumPersistence.update(album);

		// Asset

		updateAsset(
			userId, album, serviceContext.getAssetCategoryIds(),
			serviceContext.getAssetTagNames());

		return album;
	}

	public void updateAsset(
			long userId, Album album, long[] assetCategoryIds,
			String[] assetTagNames)
		throws PortalException, SystemException {

		assetEntryLocalService.updateEntry(
			userId, album.getGroupId(), album.getCreateDate(),
			album.getModifiedDate(), Album.class.getName(),
			album.getAlbumId(), album.getUuid(), 0, assetCategoryIds,
			assetTagNames, true, null, null, null, ContentTypes.TEXT_HTML,
			album.getName(), null, null, null, null, 0, 0, null, false);
	}

	protected void validate(String name) throws PortalException {
		if (Validator.isNull(name)) {
			throw new AlbumNameException();
		}
	}
}