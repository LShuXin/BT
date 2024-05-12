//
//  BTSearchModule.m
//

#import "BTSearchModule.h"
#import "BTDatabaseUtil.h"
#import "BTGroupModule.h"
#import "BTUserEntity.h"
#import "BTGroupEntity.h"
#import "BTSpellLibrary.h"


@interface BTSearchModule(PrivateAPI)
-(NSArray *)p_getAllUsersAndGroups;
-(NSString *)p_getIdForObject:(id)sender;
@end


@implementation BTSearchModule
{
    NSArray *_allUsersAndGroups;
}

+(instancetype)instance
{
    static BTSearchModule *g_searchModule;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        g_searchModule = [[BTSearchModule alloc] init];
    });
    return g_searchModule;
}

#pragma mark - Public API
-(void)searchContent:(NSString *)content completion:(SearchCompletion)completion
{
    content = [content lowercaseString];
    [[BTSundriesCenter instance] pushTaskToSerialQueue:^{
        NSMutableArray *matchesIdArray = [[NSMutableArray alloc] init];
        if (!_allUsersAndGroups || [_allUsersAndGroups count] == 0)
        {
            _allUsersAndGroups = [self p_getAllUsersAndGroups];
        }
        
        NSMutableArray *matches = NULL;
        NSUInteger i, count;
        NSString *string;
        
        count         = [_allUsersAndGroups count];
        matches       = [NSMutableArray array];
        
        // find any match in our keyword array against what was typed -
        for (i = 0; i < count; i++)
        {
            NSObject *user = [_allUsersAndGroups objectAtIndex:i];
            string = [(BTUserEntity *)user nick];
            NSString *objectId = [self p_getIdForObject:user];

            if ([string rangeOfString:content].length > 0)
            {
                if (![matches containsObject:user])
                {
                    [matches addObject:user];
                    [matchesIdArray addObject:objectId];
                }
            }
        }
        
        NSString *partialSpell = [[BTSpellLibrary instance] getSpellForWord:content];
        NSArray *userInSpellLibaray = [[BTSpellLibrary instance] checkoutForWordsForSpell:partialSpell];
        
        if ([userInSpellLibaray count] > 0)
        {
            [userInSpellLibaray enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                NSString* objectId = [self p_getIDForObject:obj];
                if (!objectId)
                {
                    return;
                }
                if (![matches containsObject:obj] && ![matchesIdArray containsObject:objectId]) {
                    [matches addObject:obj];
                    [matchesIdArray addObject:objectId];
                }
            }];
        }
        
        [matches sortUsingComparator:^NSComparisonResult(id obj1, id obj2) {
            if ([obj1 isKindOfClass:[BTUserEntity class]])
            {
                return NSOrderedAscending;
            }
            else
            {
                return NSOrderedDescending;
            }
        }];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            completion(matches, nil);
        });
    }];
}

- (void)searchDepartment:(NSString*)content completion:(SearchCompletion)completion
{
    content = [content lowercaseString];
    [[BTSundriesCenter instance] pushTaskToSerialQueue:^{
        NSMutableArray* matchesIDArray = [[NSMutableArray alloc] init];
        if (!_allUsersAndGroups || [_allUsersAndGroups count] == 0)
        {
            _allUsersAndGroups = [self p_getAllUsersAndGroups];
        }
        NSMutableArray*	matches = NULL;
        NSUInteger	i,count;
        NSString*		string;
        
        count         = [_allUsersAndGroups count];
        matches       = [NSMutableArray array];
        
        // find any match in our keyword array against what was typed -
        for (i=0; i< count; i++)
        {
            NSObject* user = [_allUsersAndGroups objectAtIndex:i];
            string = [(BTUserEntity*)user department];
            NSString* objectID = [self p_getIDForObject:user];
            
            if ([string rangeOfString:content].length > 0)
            {
                if (![matches containsObject:user])
                {
                    [matches addObject:user];
                    [matchesIDArray addObject:objectID];
                }
            }
        }
        NSString* partialSpell = [[BTSpellLibrary instance] getSpellForWord:content];
        NSArray* userInSpellLibaray = [[BTSpellLibrary instance] checkoutForWordsForSpell_Deparment:partialSpell];
        
        if ([userInSpellLibaray count] > 0)
        {
            [userInSpellLibaray enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
                NSString* objectID = [self p_getIDForObject:obj];
                if (!objectID)
                {
                    return;
                }
                if (![matches containsObject:obj] && ![matchesIDArray containsObject:objectID]) {
                    [matches addObject:obj];
                    [matchesIDArray addObject:objectID];
                }
            }];
        }
        
        [matches sortUsingComparator:^NSComparisonResult(id obj1, id obj2) {
            if ([obj1 isKindOfClass:[BTUserEntity class]])
            {
                return NSOrderedAscending;
            }
            else
            {
                return NSOrderedDescending;
            }
        }];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            completion(matches,nil);
        });
    }];
}
- (void)searchContent:(NSString *)content inRange:(NSArray*)ranges completion:(SearchCompletion)completion
{
    [[BTSundriesCenter instance] pushTaskToSerialQueue:^{
        NSUInteger	i,count;
        NSString*		string;
        
        count         = [ranges count];
        NSMutableArray* matches = [[NSMutableArray alloc] init];
        
        // find any match in our keyword array against what was typed -
        for (i=0; i< count; i++)
        {
            BTUserEntity* user = [ranges objectAtIndex:i];
            string = user.nick;
            if ([string rangeOfString:content].length > 0)
            {
                if (![matches containsObject:user])
                {
                    [matches addObject:user];
                }
            }
        }
        NSString* partialSpell = [[BTSpellLibrary instance] getSpellForWord:content];
        NSArray* userInSpellLibaray = [[BTSpellLibrary instance] checkoutForWordsForSpell:partialSpell];
        
        NSMutableArray* rangsIDs = [[NSMutableArray alloc] init];
        [ranges enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            NSString* ID = [self p_getIDForObject:obj];
            [rangsIDs addObject:ID];
        }];
        
        if ([userInSpellLibaray count] > 0)
        {
            for (NSInteger index = 0; index < [userInSpellLibaray count]; index ++)
            {
                id object = userInSpellLibaray[index];
                NSString* objectID = [self p_getIDForObject:object];
                if (!objectID) {
                    continue;
                }
                if (![matches containsObject:object] && [rangsIDs containsObject:objectID]) {
                    [matches addObject:object];
                }
            }
//            [userInSpellLibaray enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
//                NSString* objectID = [self p_getIDForObject:obj];
//                if (!objectID)
//                {
//                    return;
//                }
//                if (![matches containsObject:obj] && [ranges containsObject:obj]) {
//                    [matches addObject:[obj copy]];
//                }
//            }];
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            completion(matches,nil);
        });
    }];
}

#pragma mark PrivateAPI
- (NSArray*)p_getAllUsersAndGroups
{
    //导入所有的用户
   __block NSMutableArray* allSessions = [NSMutableArray new];
    dispatch_semaphore_t sema = dispatch_semaphore_create(0);
    [[BTDatabaseUtil instance] getAllContacts:^(NSArray *contacts, NSError *error) {
        BTLog(@"LLLLLLLLLL%@", [contacts count]);
        BTLog(@"LLLLLLLLLL%@", [contacts count]);
        BTLog(@"LLLLLLLLLL%@", [contacts count]);
        BTLog(@"LLLLLLLLLL%@", [contacts count]);
        [allSessions addObjectsFromArray:contacts];
        dispatch_semaphore_signal(sema);
    }];
     dispatch_semaphore_wait(sema, DISPATCH_TIME_FOREVER);
    return allSessions;
}

- (NSString*)p_getIDForObject:(id)sender
{
    NSString* objectID = nil;
    if ([sender isKindOfClass:[BTUserEntity class]])
    {
        objectID = [(BTUserEntity*)sender objId];
    }
    else if ([sender isKindOfClass:[BTGroupEntity class]])
    {
        objectID = [(BTGroupEntity*)sender objId];
    }

    return objectID;
}
@end
