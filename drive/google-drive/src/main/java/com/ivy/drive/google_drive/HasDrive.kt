package com.ivy.drive.google_drive

import arrow.core.Either
import com.google.api.services.drive.Drive
import com.ivy.drive.google_drive.data.GoogleDriveError

interface HasDrive {
    val errorOrDrive: Either<GoogleDriveError, Drive>
}