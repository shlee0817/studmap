using Elmah;
using StudMap.Core;
using StudMap.Core.Users;
using StudMap.Data.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web.Helpers;

namespace StudMap.Service.Services
{
    public class UserService
    {
        public static void Register(UserEntities entities, string userName, string password)
        {
            if (ExistsUserAlready(userName, entities))
                throw new ServiceException(ResponseError.UserNameDuplicate);

            CreateUserAndAccount(userName, password, entities);
        }

        public static void Login(UserEntities entities, string userName, string password)
        {
            UserProfile user = entities.UserProfile.FirstOrDefault(u => u.UserName == userName);
            if (user == null)
                throw new ServiceException(ResponseError.LoginInvalid);

            webpages_Membership membership = entities.webpages_Membership.First(m => m.UserId == user.UserId);
            if (!Crypto.VerifyHashedPassword(membership.Password, password))
                throw new ServiceException(ResponseError.LoginInvalid);

            ActiveUsers activeUser = entities.ActiveUsers.FirstOrDefault(u => u.UserId == user.UserId);
            if (activeUser != null)
                activeUser.LoginDate = DateTime.Now;
            else
                entities.ActiveUsers.Add(new ActiveUsers { UserId = user.UserId, LoginDate = DateTime.Now });

            entities.SaveChanges();
        }

        public static void Logout(UserEntities entities, string userName)
        {
            UserProfile user = entities.UserProfile.FirstOrDefault(u => u.UserName == userName);
            if (user == null)
                throw new ServiceException(ResponseError.LoginInvalid);

            ActiveUsers activeUser = entities.ActiveUsers.FirstOrDefault(u => u.UserId == user.UserId);
            if (activeUser != null)
            {
                entities.ActiveUsers.Remove(activeUser);
                entities.SaveChanges();
            }
        }

        public static List<User> GetActiveUsers(UserEntities entities)
        {
            return entities.ActiveUsers.Select(activeUser => new User
                {
                    Name = entities.UserProfile.First(u => u.UserId == activeUser.UserId).UserName
                }).ToList();
        }

        /// <summary>
        /// Nach dieser Zeit wird ein aktiver Benutzer als inaktiv erkannt und abgemeldet.
        /// </summary>
        private const int ActiveUserTimeoutSeconds = 15 * 60;

        public static void CheckActiveUsers()
        {
            try
            {
                using (var entities = new UserEntities())
                {
                    var timeout = DateTime.Now.AddSeconds(-ActiveUserTimeoutSeconds);
                    var inactiveUsers = entities.ActiveUsers.Where(u => u.LoginDate < timeout);

                    entities.ActiveUsers.RemoveRange(inactiveUsers);
                    entities.SaveChanges();
                }
            }
            catch (Exception ex)
            {
                ErrorSignal.FromCurrentContext().Raise(ex);
            }
        }

        private static bool ExistsUserAlready(string userName, UserEntities entities)
        {
            return entities.UserProfile.Any(profile => profile.UserName == userName);
        }

        private static void CreateUserAndAccount(string userName, string password, UserEntities entities)
        {
            UserProfile user = entities.UserProfile.Add(new UserProfile
            {
                UserName = userName,
                webpages_Roles =
                    new List<webpages_Roles> { entities.webpages_Roles.First(role => role.RoleName == "Users") }
            });
            entities.SaveChanges();

            entities.webpages_Membership.Add(new webpages_Membership
            {
                UserId = user.UserId,
                CreateDate = DateTime.Now,
                IsConfirmed = true,
                Password = Crypto.HashPassword(password),
                PasswordSalt = string.Empty,
                PasswordChangedDate = DateTime.Now,
            });
            entities.SaveChanges();
        }
    }
}