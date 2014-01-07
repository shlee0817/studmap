USE [master]
GO
/****** Object:  Database [StudMap]    Script Date: 07.01.2014 16:09:00 ******/
CREATE DATABASE [StudMap]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'StudMap_Data', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL11.STUDMAPDB\MSSQL\DATA\StudMap_Data.mdf' , SIZE = 10112KB , MAXSIZE = UNLIMITED, FILEGROWTH = 10%)
 LOG ON 
( NAME = N'StudMap_Log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL11.STUDMAPDB\MSSQL\DATA\StudMap_Log.ldf' , SIZE = 2048KB , MAXSIZE = 2048GB , FILEGROWTH = 1024KB )
GO
ALTER DATABASE [StudMap] SET COMPATIBILITY_LEVEL = 110
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [StudMap].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [StudMap] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [StudMap] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [StudMap] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [StudMap] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [StudMap] SET ARITHABORT OFF 
GO
ALTER DATABASE [StudMap] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [StudMap] SET AUTO_CREATE_STATISTICS ON 
GO
ALTER DATABASE [StudMap] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [StudMap] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [StudMap] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [StudMap] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [StudMap] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [StudMap] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [StudMap] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [StudMap] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [StudMap] SET  ENABLE_BROKER 
GO
ALTER DATABASE [StudMap] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [StudMap] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [StudMap] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [StudMap] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [StudMap] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [StudMap] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [StudMap] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [StudMap] SET RECOVERY FULL 
GO
ALTER DATABASE [StudMap] SET  MULTI_USER 
GO
ALTER DATABASE [StudMap] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [StudMap] SET DB_CHAINING OFF 
GO
ALTER DATABASE [StudMap] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [StudMap] SET TARGET_RECOVERY_TIME = 0 SECONDS 
GO
EXEC sys.sp_db_vardecimal_storage_format N'StudMap', N'ON'
GO
USE [StudMap]
GO
/****** Object:  User [StudMapDBUser]    Script Date: 07.01.2014 16:09:01 ******/
CREATE USER [StudMapDBUser] FOR LOGIN [StudMapDBUser] WITH DEFAULT_SCHEMA=[db_datareader]
GO
/****** Object:  User [StudMapDBAdmin]    Script Date: 07.01.2014 16:09:01 ******/
CREATE USER [StudMapDBAdmin] FOR LOGIN [StudMapDBAdmin] WITH DEFAULT_SCHEMA=[dbo]
GO
ALTER ROLE [db_datareader] ADD MEMBER [StudMapDBUser]
GO
ALTER ROLE [db_datawriter] ADD MEMBER [StudMapDBUser]
GO
ALTER ROLE [db_owner] ADD MEMBER [StudMapDBAdmin]
GO
/****** Object:  StoredProcedure [dbo].[sp_DeleteFloor]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- Author:		Thomas Buning
-- Create date: 08.11.2013
-- Description:	Loescht einen Floor aus der Datenbank
-- =============================================
CREATE PROCEDURE [dbo].[sp_DeleteFloor]
	@FloorId int = 0
AS
BEGIN
	SET NOCOUNT ON;

	IF @FloorId > 0
	BEGIN
		
		exec sp_DeleteGraphFromFloor @FloorId = @FloorId

		DELETE FROM Floors
		WHERE Id = @FloorId

	END
END


GO
/****** Object:  StoredProcedure [dbo].[sp_DeleteGraphFromFloor]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- Author:		Thomas Buning
-- Create date: 19.11.2013
-- Description:	Löscht alle Informationen eines Graphen zu einem Floor
-- =============================================
CREATE PROCEDURE [dbo].[sp_DeleteGraphFromFloor]
	@FloorId int = 0
AS
BEGIN
	SET NOCOUNT ON;

   IF @FloorId > 0
	BEGIN
		
		DELETE FROM NodeInformation
		WHERE NodeId IN
		(
			SELECT n.Id
			FROM Nodes n
			WHERE n.FloorId = @FloorId
		)

		DELETE FROM PoIs
		WHERE Id IN 
		(
			SELECT ni.PoiId
			FROM NodeInformation ni
				LEFT JOIN Nodes n ON n.Id = ni.NodeId
			WHERE n.FloorId = @FloorId
		)

		DELETE FROM Edges
		WHERE NodeStartId IN
		(
			SELECT n.Id
			FROM Nodes n
			WHERE n.FloorId = @FloorId
		) OR NodeEndId IN 
		(
			SELECT n.Id
			FROM Nodes n
			WHERE n.FloorId = @FloorId
		)

		DELETE FROM Nodes
		WHERE FloorId = @FloorId
	END
END


GO
/****** Object:  StoredProcedure [dbo].[sp_DeleteMap]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

-- =============================================
-- Author:		Thomas Buning
-- Create date: 08.11.2013
-- Description:	Loescht zu einer Karte alle Elemente aus der Datenbank
-- =============================================
CREATE PROCEDURE [dbo].[sp_DeleteMap]

	@MapId int = 0

AS
BEGIN
	SET NOCOUNT ON;

	IF @MapId <> 0
	BEGIN
		--Es muss der Graph geloescht werden
		DELETE FROM Edges
		WHERE GraphId IN
		(
			SELECT g.Id
			FROM Graphs g
			WHERE g.MapId = @MapId 
		)

		DELETE FROM Graphs
		WHERE MapId = @MapId

		--Floors, Nodes und NodeInformationen loeschen
		DECLARE @id int;

		DECLARE floors_cursor CURSOR FAST_FORWARD FOR
		SELECT f.Id
		FROM Floors f
		WHERE f.MapId = @MapId 

		OPEN floors_cursor
		FETCH NEXT FROM floors_cursor
		INTO @id

		WHILE @@FETCH_STATUS = 0
		BEGIN
			exec sp_DeleteFloor @FloorId = @id
			
			FETCH NEXT FROM floors_cursor
			INTO @id
		END

		CLOSE floors_cursor
		
		DELETE FROM Maps
		WHERE Id = @MapId
	END
	

END


GO
/****** Object:  Table [dbo].[AccessPoints]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[AccessPoints](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[MAC] [char](12) NOT NULL,
 CONSTRAINT [PK_AccessPoints] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[AccessPointScans]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[AccessPointScans](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[FingerprintId] [int] NOT NULL,
	[AccessPointId] [int] NOT NULL,
	[RecievedSignalStrength] [int] NOT NULL,
 CONSTRAINT [PK_AccessPointScans] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[ActiveUsers]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ActiveUsers](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[UserId] [int] NOT NULL,
	[LoginDate] [datetime] NOT NULL,
 CONSTRAINT [PK_ActiveUsers] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[Edges]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Edges](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[GraphId] [int] NOT NULL,
	[NodeStartId] [int] NOT NULL,
	[NodeEndId] [int] NOT NULL,
	[CreationTime] [datetime] NOT NULL,
 CONSTRAINT [PK_Edges] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[Fingerprints]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Fingerprints](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[NodeId] [int] NOT NULL,
 CONSTRAINT [PK_Fingerprints] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[Floors]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Floors](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[MapId] [int] NOT NULL,
	[Name] [nvarchar](255) NOT NULL,
	[ImageUrl] [nvarchar](max) NOT NULL,
	[CreationTime] [datetime] NOT NULL,
 CONSTRAINT [PK_Floors] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[Graphs]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Graphs](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[MapId] [int] NOT NULL,
	[CreationTime] [datetime] NOT NULL,
 CONSTRAINT [PK_Graphs] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[Maps]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Maps](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[Name] [nvarchar](255) NULL,
	[CreationTime] [datetime] NOT NULL,
 CONSTRAINT [PK_Maps] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[NodeInformation]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[NodeInformation](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[NodeId] [int] NOT NULL,
	[DisplayName] [nvarchar](50) NULL,
	[RoomName] [nvarchar](255) NULL,
	[QRCode] [nvarchar](255) NULL,
	[NFCTag] [nvarchar](50) NULL,
	[PoiId] [int] NULL,
	[CreationTime] [datetime] NOT NULL,
 CONSTRAINT [PK_NodeInformation] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[Nodes]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Nodes](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[FloorId] [int] NOT NULL,
	[X] [decimal](18, 17) NOT NULL,
	[Y] [decimal](18, 17) NOT NULL,
	[CreationTime] [datetime] NOT NULL,
 CONSTRAINT [PK_Nodes] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[PoIs]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PoIs](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[TypeId] [int] NOT NULL,
	[Description] [nvarchar](max) NULL,
 CONSTRAINT [PK_PoIs] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[PoiTypes]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[PoiTypes](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[Name] [nvarchar](255) NULL,
 CONSTRAINT [PK_PoiTypes] PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[UserProfile]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[UserProfile](
	[UserId] [int] IDENTITY(1,1) NOT NULL,
	[UserName] [nvarchar](56) NOT NULL,
 CONSTRAINT [PK_UserProfile] PRIMARY KEY CLUSTERED 
(
	[UserId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[webpages_Membership]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[webpages_Membership](
	[UserId] [int] NOT NULL,
	[CreateDate] [datetime] NULL,
	[ConfirmationToken] [nvarchar](128) NULL,
	[IsConfirmed] [bit] NULL,
	[LastPasswordFailureDate] [datetime] NULL,
	[PasswordFailuresSinceLastSuccess] [int] NOT NULL,
	[Password] [nvarchar](128) NOT NULL,
	[PasswordChangedDate] [datetime] NULL,
	[PasswordSalt] [nvarchar](128) NOT NULL,
	[PasswordVerificationToken] [nvarchar](128) NULL,
	[PasswordVerificationTokenExpirationDate] [datetime] NULL
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[webpages_OAuthMembership]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[webpages_OAuthMembership](
	[Provider] [nvarchar](30) NOT NULL,
	[ProviderUserId] [nvarchar](100) NOT NULL,
	[UserId] [int] NOT NULL
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[webpages_Roles]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[webpages_Roles](
	[RoleId] [int] NOT NULL,
	[RoleName] [nvarchar](256) NOT NULL,
 CONSTRAINT [PK_webpages_Roles] PRIMARY KEY CLUSTERED 
(
	[RoleId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[webpages_UsersInRoles]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[webpages_UsersInRoles](
	[UserId] [int] NOT NULL,
	[RoleId] [int] NOT NULL
) ON [PRIMARY]

GO
/****** Object:  View [dbo].[NodeInformationForMap]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO

CREATE VIEW [dbo].[NodeInformationForMap]
AS
SELECT        dbo.Maps.Id AS MapId, dbo.NodeInformation.DisplayName, dbo.NodeInformation.RoomName, dbo.Nodes.Id AS NodeId, dbo.Floors.Id AS FloorId
FROM            dbo.Floors INNER JOIN
                         dbo.Maps ON dbo.Floors.MapId = dbo.Maps.Id INNER JOIN
                         dbo.Nodes ON dbo.Floors.Id = dbo.Nodes.FloorId INNER JOIN
                         dbo.NodeInformation ON dbo.Nodes.Id = dbo.NodeInformation.NodeId
WHERE        (dbo.NodeInformation.DisplayName <> N'') AND (dbo.NodeInformation.RoomName <> N'') AND (dbo.NodeInformation.DisplayName IS NOT NULL) AND 
                         (dbo.NodeInformation.RoomName IS NOT NULL)


GO
/****** Object:  View [dbo].[PoisForMap]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[PoisForMap]
AS
SELECT        dbo.Maps.Id AS MapId, dbo.PoiTypes.Id AS PoiTypeId, dbo.PoiTypes.Name AS PoiTypeName, dbo.PoIs.Id AS PoiId, dbo.PoIs.Description AS PoiDescription, 
                         dbo.NodeInformation.DisplayName, dbo.NodeInformation.RoomName, dbo.Nodes.Id AS NodeId, dbo.Floors.Id AS FloorId
FROM            dbo.PoIs INNER JOIN
                         dbo.PoiTypes ON dbo.PoIs.TypeId = dbo.PoiTypes.Id INNER JOIN
                         dbo.NodeInformation ON dbo.PoIs.Id = dbo.NodeInformation.PoiId INNER JOIN
                         dbo.Floors INNER JOIN
                         dbo.Nodes ON dbo.Floors.Id = dbo.Nodes.FloorId INNER JOIN
                         dbo.Maps ON dbo.Floors.MapId = dbo.Maps.Id ON dbo.NodeInformation.NodeId = dbo.Nodes.Id
WHERE        (dbo.NodeInformation.Id IS NOT NULL) AND (dbo.PoIs.Id IS NOT NULL)

GO
/****** Object:  View [dbo].[RoomsForMap]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[RoomsForMap]
AS
SELECT        n.Id AS NodeId, ni.DisplayName, ni.RoomName, f.Id AS FloorId
FROM            dbo.Maps AS m LEFT OUTER JOIN
                         dbo.Floors AS f ON f.MapId = m.Id LEFT OUTER JOIN
                         dbo.Nodes AS n ON n.FloorId = f.Id LEFT OUTER JOIN
                         dbo.NodeInformation AS ni ON n.Id = ni.NodeId
WHERE        (ni.DisplayName <> '') AND (ni.Id IS NOT NULL) AND (ni.DisplayName IS NOT NULL) OR
                         (ni.Id IS NOT NULL) AND (ni.RoomName <> '') AND (ni.RoomName IS NOT NULL)

GO
/****** Object:  View [dbo].[RSSDistribution]    Script Date: 07.01.2014 16:09:02 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[RSSDistribution]
AS
SELECT f.NodeId, aps.AccessPointId, AVG(aps.RecievedSignalStrength) AS AvgRSS, STDEV(aps.RecievedSignalStrength) AS StDevRSS
FROM     dbo.AccessPointScans AS aps INNER JOIN
                  dbo.Fingerprints AS f ON f.Id = aps.FingerprintId
GROUP BY f.NodeId, aps.AccessPointId
HAVING (STDEV(aps.RecievedSignalStrength) > 0.5) AND (COUNT(aps.Id) > 3)

GO
ALTER TABLE [dbo].[AccessPointScans]  WITH CHECK ADD  CONSTRAINT [FK_AccessPointScans_AccessPoints] FOREIGN KEY([AccessPointId])
REFERENCES [dbo].[AccessPoints] ([Id])
GO
ALTER TABLE [dbo].[AccessPointScans] CHECK CONSTRAINT [FK_AccessPointScans_AccessPoints]
GO
ALTER TABLE [dbo].[AccessPointScans]  WITH CHECK ADD  CONSTRAINT [FK_AccessPointScans_Fingerprints] FOREIGN KEY([FingerprintId])
REFERENCES [dbo].[Fingerprints] ([Id])
GO
ALTER TABLE [dbo].[AccessPointScans] CHECK CONSTRAINT [FK_AccessPointScans_Fingerprints]
GO
ALTER TABLE [dbo].[ActiveUsers]  WITH CHECK ADD  CONSTRAINT [FK_ActiveUsers_UserProfile] FOREIGN KEY([UserId])
REFERENCES [dbo].[UserProfile] ([UserId])
GO
ALTER TABLE [dbo].[ActiveUsers] CHECK CONSTRAINT [FK_ActiveUsers_UserProfile]
GO
ALTER TABLE [dbo].[Edges]  WITH CHECK ADD  CONSTRAINT [FK_Edges_Graphs] FOREIGN KEY([GraphId])
REFERENCES [dbo].[Graphs] ([Id])
GO
ALTER TABLE [dbo].[Edges] CHECK CONSTRAINT [FK_Edges_Graphs]
GO
ALTER TABLE [dbo].[Edges]  WITH CHECK ADD  CONSTRAINT [FK_Edges_Nodes] FOREIGN KEY([NodeStartId])
REFERENCES [dbo].[Nodes] ([Id])
GO
ALTER TABLE [dbo].[Edges] CHECK CONSTRAINT [FK_Edges_Nodes]
GO
ALTER TABLE [dbo].[Edges]  WITH CHECK ADD  CONSTRAINT [FK_Edges_Nodes1] FOREIGN KEY([NodeEndId])
REFERENCES [dbo].[Nodes] ([Id])
GO
ALTER TABLE [dbo].[Edges] CHECK CONSTRAINT [FK_Edges_Nodes1]
GO
ALTER TABLE [dbo].[Fingerprints]  WITH CHECK ADD  CONSTRAINT [FK_Fingerprints_Nodes] FOREIGN KEY([NodeId])
REFERENCES [dbo].[Nodes] ([Id])
GO
ALTER TABLE [dbo].[Fingerprints] CHECK CONSTRAINT [FK_Fingerprints_Nodes]
GO
ALTER TABLE [dbo].[Floors]  WITH CHECK ADD  CONSTRAINT [FK_Floors_Maps] FOREIGN KEY([MapId])
REFERENCES [dbo].[Maps] ([Id])
GO
ALTER TABLE [dbo].[Floors] CHECK CONSTRAINT [FK_Floors_Maps]
GO
ALTER TABLE [dbo].[Graphs]  WITH CHECK ADD  CONSTRAINT [FK_Graphs_Maps] FOREIGN KEY([MapId])
REFERENCES [dbo].[Maps] ([Id])
GO
ALTER TABLE [dbo].[Graphs] CHECK CONSTRAINT [FK_Graphs_Maps]
GO
ALTER TABLE [dbo].[NodeInformation]  WITH CHECK ADD  CONSTRAINT [FK_NodeInformation_Nodes] FOREIGN KEY([NodeId])
REFERENCES [dbo].[Nodes] ([Id])
GO
ALTER TABLE [dbo].[NodeInformation] CHECK CONSTRAINT [FK_NodeInformation_Nodes]
GO
ALTER TABLE [dbo].[NodeInformation]  WITH CHECK ADD  CONSTRAINT [FK_NodeInformation_PoIs] FOREIGN KEY([PoiId])
REFERENCES [dbo].[PoIs] ([Id])
GO
ALTER TABLE [dbo].[NodeInformation] CHECK CONSTRAINT [FK_NodeInformation_PoIs]
GO
ALTER TABLE [dbo].[Nodes]  WITH CHECK ADD  CONSTRAINT [FK_Nodes_Floors] FOREIGN KEY([FloorId])
REFERENCES [dbo].[Floors] ([Id])
GO
ALTER TABLE [dbo].[Nodes] CHECK CONSTRAINT [FK_Nodes_Floors]
GO
ALTER TABLE [dbo].[PoIs]  WITH CHECK ADD  CONSTRAINT [FK_PoIs_PoiTypes] FOREIGN KEY([TypeId])
REFERENCES [dbo].[PoiTypes] ([Id])
GO
ALTER TABLE [dbo].[PoIs] CHECK CONSTRAINT [FK_PoIs_PoiTypes]
GO
ALTER TABLE [dbo].[webpages_Membership]  WITH CHECK ADD  CONSTRAINT [FK_webpages_Membership_UserProfile] FOREIGN KEY([UserId])
REFERENCES [dbo].[UserProfile] ([UserId])
GO
ALTER TABLE [dbo].[webpages_Membership] CHECK CONSTRAINT [FK_webpages_Membership_UserProfile]
GO
ALTER TABLE [dbo].[webpages_OAuthMembership]  WITH CHECK ADD  CONSTRAINT [FK_webpages_OAuthMembership_UserProfile] FOREIGN KEY([UserId])
REFERENCES [dbo].[UserProfile] ([UserId])
GO
ALTER TABLE [dbo].[webpages_OAuthMembership] CHECK CONSTRAINT [FK_webpages_OAuthMembership_UserProfile]
GO
ALTER TABLE [dbo].[webpages_UsersInRoles]  WITH CHECK ADD  CONSTRAINT [FK_webpages_UsersInRoles_UserProfile] FOREIGN KEY([UserId])
REFERENCES [dbo].[UserProfile] ([UserId])
GO
ALTER TABLE [dbo].[webpages_UsersInRoles] CHECK CONSTRAINT [FK_webpages_UsersInRoles_UserProfile]
GO
ALTER TABLE [dbo].[webpages_UsersInRoles]  WITH CHECK ADD  CONSTRAINT [FK_webpages_UsersInRoles_webpages_Roles] FOREIGN KEY([RoleId])
REFERENCES [dbo].[webpages_Roles] ([RoleId])
GO
ALTER TABLE [dbo].[webpages_UsersInRoles] CHECK CONSTRAINT [FK_webpages_UsersInRoles_webpages_Roles]
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPane1', @value=N'[0E232FF0-B466-11cf-A24F-00AA00A3EFFF, 1.00]
Begin DesignProperties = 
   Begin PaneConfigurations = 
      Begin PaneConfiguration = 0
         NumPanes = 4
         Configuration = "(H (1[40] 4[20] 2[20] 3) )"
      End
      Begin PaneConfiguration = 1
         NumPanes = 3
         Configuration = "(H (1 [50] 4 [25] 3))"
      End
      Begin PaneConfiguration = 2
         NumPanes = 3
         Configuration = "(H (1 [50] 2 [25] 3))"
      End
      Begin PaneConfiguration = 3
         NumPanes = 3
         Configuration = "(H (4 [30] 2 [40] 3))"
      End
      Begin PaneConfiguration = 4
         NumPanes = 2
         Configuration = "(H (1 [56] 3))"
      End
      Begin PaneConfiguration = 5
         NumPanes = 2
         Configuration = "(H (2 [66] 3))"
      End
      Begin PaneConfiguration = 6
         NumPanes = 2
         Configuration = "(H (4 [50] 3))"
      End
      Begin PaneConfiguration = 7
         NumPanes = 1
         Configuration = "(V (3))"
      End
      Begin PaneConfiguration = 8
         NumPanes = 3
         Configuration = "(H (1[56] 4[18] 2) )"
      End
      Begin PaneConfiguration = 9
         NumPanes = 2
         Configuration = "(H (1 [75] 4))"
      End
      Begin PaneConfiguration = 10
         NumPanes = 2
         Configuration = "(H (1[66] 2) )"
      End
      Begin PaneConfiguration = 11
         NumPanes = 2
         Configuration = "(H (4 [60] 2))"
      End
      Begin PaneConfiguration = 12
         NumPanes = 1
         Configuration = "(H (1) )"
      End
      Begin PaneConfiguration = 13
         NumPanes = 1
         Configuration = "(V (4))"
      End
      Begin PaneConfiguration = 14
         NumPanes = 1
         Configuration = "(V (2))"
      End
      ActivePaneConfig = 0
   End
   Begin DiagramPane = 
      Begin Origin = 
         Top = 0
         Left = 0
      End
      Begin Tables = 
         Begin Table = "Floors"
            Begin Extent = 
               Top = 6
               Left = 38
               Bottom = 135
               Right = 208
            End
            DisplayFlags = 280
            TopColumn = 0
         End
         Begin Table = "Maps"
            Begin Extent = 
               Top = 6
               Left = 246
               Bottom = 118
               Right = 416
            End
            DisplayFlags = 280
            TopColumn = 0
         End
         Begin Table = "Nodes"
            Begin Extent = 
               Top = 6
               Left = 454
               Bottom = 135
               Right = 624
            End
            DisplayFlags = 280
            TopColumn = 0
         End
         Begin Table = "NodeInformation"
            Begin Extent = 
               Top = 120
               Left = 246
               Bottom = 286
               Right = 416
            End
            DisplayFlags = 280
            TopColumn = 0
         End
      End
   End
   Begin SQLPane = 
   End
   Begin DataPane = 
      Begin ParameterDefaults = ""
      End
   End
   Begin CriteriaPane = 
      Begin ColumnWidths = 11
         Column = 1860
         Alias = 1425
         Table = 1170
         Output = 720
         Append = 1400
         NewValue = 1170
         SortType = 1350
         SortOrder = 1410
         GroupBy = 1350
         Filter = 1350
         Or = 1350
         Or = 1350
         Or = 1350
      End
   End
End
' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'NodeInformationForMap'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPaneCount', @value=1 , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'NodeInformationForMap'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPane1', @value=N'[0E232FF0-B466-11cf-A24F-00AA00A3EFFF, 1.00]
Begin DesignProperties = 
   Begin PaneConfigurations = 
      Begin PaneConfiguration = 0
         NumPanes = 4
         Configuration = "(H (1[40] 4[20] 2[20] 3) )"
      End
      Begin PaneConfiguration = 1
         NumPanes = 3
         Configuration = "(H (1 [50] 4 [25] 3))"
      End
      Begin PaneConfiguration = 2
         NumPanes = 3
         Configuration = "(H (1 [50] 2 [25] 3))"
      End
      Begin PaneConfiguration = 3
         NumPanes = 3
         Configuration = "(H (4 [30] 2 [40] 3))"
      End
      Begin PaneConfiguration = 4
         NumPanes = 2
         Configuration = "(H (1 [56] 3))"
      End
      Begin PaneConfiguration = 5
         NumPanes = 2
         Configuration = "(H (2 [66] 3))"
      End
      Begin PaneConfiguration = 6
         NumPanes = 2
         Configuration = "(H (4 [50] 3))"
      End
      Begin PaneConfiguration = 7
         NumPanes = 1
         Configuration = "(V (3))"
      End
      Begin PaneConfiguration = 8
         NumPanes = 3
         Configuration = "(H (1[56] 4[18] 2) )"
      End
      Begin PaneConfiguration = 9
         NumPanes = 2
         Configuration = "(H (1 [75] 4))"
      End
      Begin PaneConfiguration = 10
         NumPanes = 2
         Configuration = "(H (1[66] 2) )"
      End
      Begin PaneConfiguration = 11
         NumPanes = 2
         Configuration = "(H (4 [60] 2))"
      End
      Begin PaneConfiguration = 12
         NumPanes = 1
         Configuration = "(H (1) )"
      End
      Begin PaneConfiguration = 13
         NumPanes = 1
         Configuration = "(V (4))"
      End
      Begin PaneConfiguration = 14
         NumPanes = 1
         Configuration = "(V (2))"
      End
      ActivePaneConfig = 0
   End
   Begin DiagramPane = 
      Begin Origin = 
         Top = -192
         Left = 0
      End
      Begin Tables = 
         Begin Table = "PoIs"
            Begin Extent = 
               Top = 92
               Left = 630
               Bottom = 204
               Right = 800
            End
            DisplayFlags = 280
            TopColumn = 0
         End
         Begin Table = "PoiTypes"
            Begin Extent = 
               Top = 86
               Left = 856
               Bottom = 181
               Right = 1026
            End
            DisplayFlags = 280
            TopColumn = 0
         End
         Begin Table = "NodeInformation"
            Begin Extent = 
               Top = 179
               Left = 463
               Bottom = 308
               Right = 633
            End
            DisplayFlags = 280
            TopColumn = 0
         End
         Begin Table = "Floors"
            Begin Extent = 
               Top = 200
               Left = 185
               Bottom = 329
               Right = 355
            End
            DisplayFlags = 280
            TopColumn = 0
         End
         Begin Table = "Nodes"
            Begin Extent = 
               Top = 27
               Left = 299
               Bottom = 156
               Right = 469
            End
            DisplayFlags = 280
            TopColumn = 0
         End
         Begin Table = "Maps"
            Begin Extent = 
               Top = 27
               Left = 34
               Bottom = 139
               Right = 204
            End
            DisplayFlags = 280
            TopColumn = 0
         End
      End
   End
   Begin SQLPane = 
   End
   Begin DataPane = 
      Begin ParameterDefaults = ""
      End
   End
   Begin CriteriaPane = 
      Begin ColumnWidths = 11
         Column = 1440
         ' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'PoisForMap'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPane2', @value=N'Alias = 900
         Table = 1170
         Output = 720
         Append = 1400
         NewValue = 1170
         SortType = 1350
         SortOrder = 1410
         GroupBy = 1350
         Filter = 1350
         Or = 1350
         Or = 1350
         Or = 1350
      End
   End
End
' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'PoisForMap'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPaneCount', @value=2 , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'PoisForMap'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPane1', @value=N'[0E232FF0-B466-11cf-A24F-00AA00A3EFFF, 1.00]
Begin DesignProperties = 
   Begin PaneConfigurations = 
      Begin PaneConfiguration = 0
         NumPanes = 4
         Configuration = "(H (1[40] 4[20] 2[20] 3) )"
      End
      Begin PaneConfiguration = 1
         NumPanes = 3
         Configuration = "(H (1 [50] 4 [25] 3))"
      End
      Begin PaneConfiguration = 2
         NumPanes = 3
         Configuration = "(H (1 [50] 2 [25] 3))"
      End
      Begin PaneConfiguration = 3
         NumPanes = 3
         Configuration = "(H (4 [30] 2 [40] 3))"
      End
      Begin PaneConfiguration = 4
         NumPanes = 2
         Configuration = "(H (1 [56] 3))"
      End
      Begin PaneConfiguration = 5
         NumPanes = 2
         Configuration = "(H (2 [66] 3))"
      End
      Begin PaneConfiguration = 6
         NumPanes = 2
         Configuration = "(H (4 [50] 3))"
      End
      Begin PaneConfiguration = 7
         NumPanes = 1
         Configuration = "(V (3))"
      End
      Begin PaneConfiguration = 8
         NumPanes = 3
         Configuration = "(H (1[56] 4[18] 2) )"
      End
      Begin PaneConfiguration = 9
         NumPanes = 2
         Configuration = "(H (1 [75] 4))"
      End
      Begin PaneConfiguration = 10
         NumPanes = 2
         Configuration = "(H (1[66] 2) )"
      End
      Begin PaneConfiguration = 11
         NumPanes = 2
         Configuration = "(H (4 [60] 2))"
      End
      Begin PaneConfiguration = 12
         NumPanes = 1
         Configuration = "(H (1) )"
      End
      Begin PaneConfiguration = 13
         NumPanes = 1
         Configuration = "(V (4))"
      End
      Begin PaneConfiguration = 14
         NumPanes = 1
         Configuration = "(V (2))"
      End
      ActivePaneConfig = 0
   End
   Begin DiagramPane = 
      Begin Origin = 
         Top = 0
         Left = 0
      End
      Begin Tables = 
         Begin Table = "m"
            Begin Extent = 
               Top = 6
               Left = 246
               Bottom = 118
               Right = 416
            End
            DisplayFlags = 280
            TopColumn = 0
         End
         Begin Table = "f"
            Begin Extent = 
               Top = 6
               Left = 38
               Bottom = 135
               Right = 208
            End
            DisplayFlags = 280
            TopColumn = 1
         End
         Begin Table = "n"
            Begin Extent = 
               Top = 138
               Left = 38
               Bottom = 267
               Right = 208
            End
            DisplayFlags = 280
            TopColumn = 0
         End
         Begin Table = "ni"
            Begin Extent = 
               Top = 120
               Left = 246
               Bottom = 305
               Right = 416
            End
            DisplayFlags = 280
            TopColumn = 0
         End
      End
   End
   Begin SQLPane = 
   End
   Begin DataPane = 
      Begin ParameterDefaults = ""
      End
   End
   Begin CriteriaPane = 
      Begin ColumnWidths = 11
         Column = 1440
         Alias = 900
         Table = 1170
         Output = 720
         Append = 1400
         NewValue = 1170
         SortType = 1350
         SortOrder = 1410
         GroupBy = 1350
         Filter = 1350
         Or = 1350
         Or = 1350
         Or = 1350
      End
   End
End
' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'RoomsForMap'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPaneCount', @value=1 , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'RoomsForMap'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPane1', @value=N'[0E232FF0-B466-11cf-A24F-00AA00A3EFFF, 1.00]
Begin DesignProperties = 
   Begin PaneConfigurations = 
      Begin PaneConfiguration = 0
         NumPanes = 4
         Configuration = "(H (1[40] 4[20] 2[20] 3) )"
      End
      Begin PaneConfiguration = 1
         NumPanes = 3
         Configuration = "(H (1 [50] 4 [25] 3))"
      End
      Begin PaneConfiguration = 2
         NumPanes = 3
         Configuration = "(H (1 [50] 2 [25] 3))"
      End
      Begin PaneConfiguration = 3
         NumPanes = 3
         Configuration = "(H (4 [30] 2 [40] 3))"
      End
      Begin PaneConfiguration = 4
         NumPanes = 2
         Configuration = "(H (1 [56] 3))"
      End
      Begin PaneConfiguration = 5
         NumPanes = 2
         Configuration = "(H (2 [66] 3))"
      End
      Begin PaneConfiguration = 6
         NumPanes = 2
         Configuration = "(H (4 [50] 3))"
      End
      Begin PaneConfiguration = 7
         NumPanes = 1
         Configuration = "(V (3))"
      End
      Begin PaneConfiguration = 8
         NumPanes = 3
         Configuration = "(H (1[56] 4[18] 2) )"
      End
      Begin PaneConfiguration = 9
         NumPanes = 2
         Configuration = "(H (1 [75] 4))"
      End
      Begin PaneConfiguration = 10
         NumPanes = 2
         Configuration = "(H (1[66] 2) )"
      End
      Begin PaneConfiguration = 11
         NumPanes = 2
         Configuration = "(H (4 [60] 2))"
      End
      Begin PaneConfiguration = 12
         NumPanes = 1
         Configuration = "(H (1) )"
      End
      Begin PaneConfiguration = 13
         NumPanes = 1
         Configuration = "(V (4))"
      End
      Begin PaneConfiguration = 14
         NumPanes = 1
         Configuration = "(V (2))"
      End
      ActivePaneConfig = 0
   End
   Begin DiagramPane = 
      Begin Origin = 
         Top = 0
         Left = 0
      End
      Begin Tables = 
         Begin Table = "aps"
            Begin Extent = 
               Top = 6
               Left = 38
               Bottom = 135
               Right = 267
            End
            DisplayFlags = 280
            TopColumn = 0
         End
         Begin Table = "f"
            Begin Extent = 
               Top = 6
               Left = 305
               Bottom = 101
               Right = 491
            End
            DisplayFlags = 280
            TopColumn = 0
         End
      End
   End
   Begin SQLPane = 
   End
   Begin DataPane = 
      Begin ParameterDefaults = ""
      End
   End
   Begin CriteriaPane = 
      Begin ColumnWidths = 12
         Column = 1440
         Alias = 900
         Table = 1176
         Output = 720
         Append = 1400
         NewValue = 1170
         SortType = 1356
         SortOrder = 1416
         GroupBy = 1350
         Filter = 1356
         Or = 1350
         Or = 1350
         Or = 1350
      End
   End
End
' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'RSSDistribution'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_DiagramPaneCount', @value=1 , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'VIEW',@level1name=N'RSSDistribution'
GO
USE [master]
GO
ALTER DATABASE [StudMap] SET  READ_WRITE 
GO
